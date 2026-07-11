package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.core.feed.FeedAttribution;
import dev.jqb.onefeed.core.feed.FeedCursor;
import dev.jqb.onefeed.core.feed.FeedId;
import dev.jqb.onefeed.instagramplugin.apimodel.author.BasicIgUserInfo;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthor;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthorDeserializer;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramCollaborator;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramCollaboratorDeserializer;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContent;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContentDeserializer;
import dev.jqb.onefeed.instagramplugin.apimodel.content.PageResult;
import dev.jqb.onefeed.instagramplugin.config.AccessToken;
import dev.jqb.onefeed.instagramplugin.config.IgFeedConfig;
import dev.jqb.onefeed.instagramplugin.config.IgProviderConfig;
import dev.jqb.onefeed.instagramplugin.config.LoginType;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Handles all requests to the Instagram API
 */
public class RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final String API_VERSION = "v25.0";

    private final HttpClient httpClient = HttpClient.newBuilder()
        .followRedirects(Redirect.NORMAL)
        .build();

    private ObjectMapper mapper;

    /**
     * The ID of the provided using this {@code RequestHandler}, for signing generated content with
     */
    private final String providerId;

    /**
     * The config of the provider using this {@code RequestHandler}
     */
    private final IgProviderConfig providerConfig;

    /**
     * Constructs a new {@code RequestHandler}.
     *
     * @param providerId the ID of the plugin using this {@code RequestHandler}, for signing generated
     *                 content with
     * @param providerConfig the config of the provider using this {@code RequestHandler}
     */
    public RequestHandler(String providerId, IgProviderConfig providerConfig) {
        this.providerId = providerId;
        this.providerConfig = providerConfig;

        SimpleModule authorModule = new SimpleModule();
        authorModule.addDeserializer(InstagramAuthor.class, new InstagramAuthorDeserializer(providerId));
        SimpleModule contentModule = new SimpleModule();
        contentModule.addDeserializer(InstagramContent.class, new InstagramContentDeserializer(providerId));
        SimpleModule collaboratorModule = new SimpleModule();
        collaboratorModule.addDeserializer(InstagramCollaborator.class, new InstagramCollaboratorDeserializer(providerId));

        this.mapper = JsonMapper.builder()
            .addModules(authorModule, contentModule, collaboratorModule)
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();
    }

    /**
     * Fetches the given {@code amount} of most recently published content from {@code this}
     * provider's content source for the given feed.
     *
     * @param feed the feed whose content to retrieve
     * @param amount the target amount of content to retrieve
     *
     * @return a {@link Flux} that emits the retrieved content
     */
    public Flux<InstagramContent> fetchRecentContent(InstagramFeed feed, int amount) {
        // Get that first page based on the cursor
        Mono<PageResult> firstPage = fetchContentPage(feed, amount, null, 0);

        // Fetch more pages of content if necessary (and possible) to fulfil the desired amount of
        // content
        Flux<PageResult> allPages = firstPage.expand(pageResult -> {
            int usableContentCount = pageResult.getContentCountAcrossPages();

            if (usableContentCount < amount && pageResult.getNextPageCursor() != null) {
                return fetchContentPage(feed, amount - usableContentCount,
                    pageResult.getNextPageCursor(), usableContentCount);
            }

            return Mono.empty();
        });

        // Take all the page data and chuck it into the cleaner, filtered content response
        return collectPageContent(allPages, feed);
    }

    /**
     * Fetches the given {@code amount} of most recently published content from {@code this}
     * provider's content source for the given feed.
     *
     * @param feed the feed whose content to retrieve
     * @param amount the target amount of content to retrieve
     * @param cursor a cursor of format {@code <cursor>-<offset>}
     *
     * @return a {@link Flux} that emits the retrieved content
     */
    public Flux<InstagramContent> fetchRecentContent(InstagramFeed feed, int amount, FeedCursor cursor) {
        // Interpret the different parts of the "cursor"... the offset (location of the first piece
        // to consider within the page) and the cursor to the page itself (cursor)

        // Get that first page based on the cursor
        Mono<PageResult> firstPage = fetchContentPage(feed, amount, cursor.getCursorOnPlatform(), cursor.getOffsetFromCursor());

        // Fetch more pages of content if necessary (and possible) to fulfil the desired amount of
        // content
        Flux<PageResult> allPages = firstPage.expand(pageResult -> {
            int usableContentCount = pageResult.getContentCountAcrossPages() - cursor.getOffsetFromCursor();

            if (usableContentCount < amount && pageResult.getNextPageCursor() != null) {
                return fetchContentPage(feed, amount - usableContentCount,
                    pageResult.getNextPageCursor(), usableContentCount);
            }

            return Mono.empty();
        });

        // Take all the page data and chuck it into the cleaner, filtered content response
        return collectPageContent(allPages, feed);
    }

    /**
     * Collect the provided list of {@link PageResult}s into a single {@link Flux} stream of
     * {@link InstagramContent}.
     *
     * @param pageResults the list of {@link PageResult}s to collect
     * @param feed the feed to which the content belongs
     *
     * @return a {@link Flux} emitting the retrieved content
     */
    public Flux<InstagramContent> collectPageContent(Flux<PageResult> pageResults,
        InstagramFeed feed
    ) {
        return pageResults.flatMap(page -> {
            List<InstagramContent> allContent = new ArrayList<>(page.getContent());

            // Complete all the content
            for (InstagramContent content : allContent) {
                content.setSource(feed.getAttribution());
            }

            return Flux.fromIterable(allContent);
        });
    }

    /**
     * Fetches the given {@code amount} of content from the specified page of content.
     *
     * @param feed the feed whose content to retrieve
     * @param amount the target amount of content to retrieve
     * @param pageCursor the cursor of the page to retrieve, or {@code null} to retrieve the first
     *                   page
     * @param currentTotal the total amount of content already retrieved thus far (cumulative sum
     *                     of this query's results, plus all prior queries)
     *
     * @return a {@link Mono} that emits the {@link PageResult}
     */
    private Mono<PageResult> fetchContentPage(InstagramFeed feed, int amount, String pageCursor,
        int currentTotal
    ) {
        URI uri = getContentUri(feed, amount, pageCursor);
        logger.debug("Fetching content page from URI: {}", uri);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        return Mono.fromCompletionStage(httpClient.sendAsync(request, BodyHandlers.ofString()))
            .map(response -> {
                logger.debug("Response:\n{}", response.body());

                JsonNode root = mapper.readTree(response.body());
                List<InstagramContent> content = mapper.treeToValue(root.get("data"),
                    new TypeReference<List<InstagramContent>>() {});

                String nextPage = root.path("paging").path("cursors")
                    .path("after").asString(null);

                if (!content.isEmpty()) {
                    content.getLast().setNextPageCursor(nextPage);
                }

                PageResult page = new PageResult(content, nextPage);
                page.setContentCountAcrossPages(currentTotal + page.getContent().size());

                return page;
                }
            );
    }

    /**
     * Gets the URI to fetch the desired content from.
     *
     * @param feed the feed whose content to retrieve
     * @param amount the target amount of content to retrieve (max 10)
     * @param pageCursor the cursor of the page to retrieve, or {@code null} to retrieve the first page
     *
     * @return the URI to fetch the desired content from
     */
    private URI getContentUri(InstagramFeed feed, int amount, String pageCursor) {
        AccessToken accessToken = feed.getConfig().getAccessToken();
        String baseUrl = getBaseUrl(feed.getConfig().getLoginType());

        String afterArg = "";
        if (pageCursor != null) {
            afterArg = "&after=" + pageCursor;
        }

        String encodedFields = URLEncoder.encode(
            "alt_text,media_type,media_url,caption,timestamp,permalink,owner," +
                "thumbnail_url,children{media_url,media_type,alt_text,thumbnail_url},"
                + "collaborators{id,invite_status}",
            StandardCharsets.UTF_8);

        if (!providerConfig.isUsingLiteFetchMode()) {
            encodedFields += ",shares_count,saved_count,reposts_count";

            if (providerConfig.shouldUseTotalMetricsForNormalization()) {
                encodedFields += ",total_views_count,total_comments_count";
            } else {
                encodedFields += ",views_count,comments_count";
            }
        }

        if (providerConfig.shouldUseTotalMetricsForNormalization()) {
            encodedFields += ",total_like_count";
        } else {
            encodedFields += ",like_count";
        }

        return URI.create(String.format(
                "%s/%s/media?access_token=%s&limit=%s%s&fields=%s",
                baseUrl, feed.getAuthor().getId(), accessToken.getValue(),
                Math.min(amount, 10), afterArg, encodedFields
            )
        );
    }

    /**
     * Gets the URI to fetch the desired author from.
     * @param feed the feed whose content to retrieve
     * @return the URI to fetch the desired author from
     */
    private URI getAuthorUri(InstagramFeed feed) {
        IgFeedConfig feedConfig = feed.getConfig();

        // Possible because the endpoint for Insta login is just "me", else it's the specific ID,
        // where both paths have the same args
        String userId = (feedConfig.getLoginType() == LoginType.FACEBOOK)
            ? feed.getAuthor().getId()
            : "me";

        String fields = "id,name,username,profile_picture_url";
        if (!providerConfig.isUsingLiteFetchMode()) {
            fields += ",followers_count,media_count,biography,website";
        }

        return URI.create(
            String.format("%s/%s?access_token=%s&fields=%s",
                getBaseUrl(feedConfig.getLoginType()), userId,
                feedConfig.getAccessToken().getValue(), fields
            )
        );
    }

    /**
     * Fetches the author responsible for the content in feed {@code feed}.
     *
     * @param feed the feed whose corresponding author data to retrieve
     * @return a {@link Mono} that emits the {@link InstagramAuthor} of the author of the desired
     * feed
     */
    public Mono<InstagramAuthor> fetchAuthor(InstagramFeed feed) {
        URI uri = getAuthorUri(feed);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        return Mono.fromCompletionStage(httpClient.sendAsync(request, BodyHandlers.ofString()))
            .map(response ->
                mapper.readValue(response.body(), InstagramAuthor.class)
            );
    }

    /**
     * Provides the Instagram User ID for the provided {@code feed}.
     *
     * @param feed the feed whose Instagram User ID to retrieve
     *
     * @return the Instagram User ID of the given {@code feed}'s author
     */
    public BasicIgUserInfo fetchInstaUserInfo(InstagramFeed feed) {
        IgFeedConfig feedConfig = feed.getConfig();

        // Get the correct URI based on the login type
        URI uri;
        if (feedConfig.getLoginType() == LoginType.FACEBOOK) {
            logger.debug("Fetching Instagram User ID from Facebook Login for Business feed: {}", feed.getId());
            String encodedFields = URLEncoder.encode("instagram_business_account{username}",
                StandardCharsets.UTF_8);
            uri = URI.create(String.format("%s/me/accounts?access_token=%s&fields=%s",
                getBaseUrl(feedConfig.getLoginType()), feedConfig.getAccessToken().getValue(), encodedFields));

        } else { // Insta login
            logger.debug("Fetching Instagram User ID from Business Login for Instagram feed: {}", feed.getId());
            uri = URI.create(String.format("%s/me?access_token=%s&fields=id,username",
                getBaseUrl(feedConfig.getLoginType()), feedConfig.getAccessToken().getValue()));
        }

        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        String responseBody = "";

        // Send the request
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new Exception("Instagram User ID retrieval response was not OK: " + response.body());
            }
            responseBody = response.body();
        } catch (Exception e) {
            logger.error("Failed to retrieve Instagram User ID", e);
        }

        // Parse the response based on the login type
        JsonNode root = mapper.readTree(responseBody);
        String id;
        String username;
        if (feedConfig.getLoginType() == LoginType.FACEBOOK) {
            id = root.get("data").asArray().get(0).get("instagram_business_account").get("id").asString();
            username = root.get("data").asArray().get(0).get("instagram_business_account").get("username").asString();
        } else { // Insta login
            id = root.get("data").asArray().get(0).get("instagram_business_account").get("id").asString();
            username = root.get("data").asArray().get(0).get("instagram_business_account").get("username").asString();
        }

        return new BasicIgUserInfo(id, username);
    }

    /**
     * Exchanges the access token for a long-lived one.
     * Intentionally synchronous as it's critical initialization.
     *
     * @param feed the whose access token to exchange for a long-lived one
     */
    public void exchangeAccessToken(InstagramFeed feed) {
        logger.debug("Attempting to exchange access token for long-lived one...");
        IgFeedConfig feedConfig = feed.getConfig();
        AccessToken accessToken = feed.getConfig().getAccessToken();

        Builder requestBuilder = HttpRequest.newBuilder();
        String clientSecret = URLEncoder.encode(providerConfig.getAppSecret(), StandardCharsets.UTF_8);
        String accessTokenStr = URLEncoder.encode(feedConfig.getAccessToken().getValue(),
            StandardCharsets.UTF_8);

        // Hit the correct endpoint with the correct args
        if (feedConfig.getLoginType() == LoginType.FACEBOOK) {
            String clientId = URLEncoder.encode(providerConfig.getAppId(), StandardCharsets.UTF_8);

            String uriString = String.format("%s/oauth/access_token?grant_type=fb_exchange_token&client_id=%s&client_secret=%s&fb_exchange_token=%s",
                getBaseUrl(feedConfig.getLoginType()), clientId, clientSecret, accessTokenStr);

            URI uri = URI.create(uriString);
            requestBuilder.uri(uri);
        } else {
            String uriString = String.format("%s/access_token?grant_type=ig_exchange_token&client_secret=%s&access_token=%s",
                getBaseUrl(feedConfig.getLoginType()), clientSecret, accessTokenStr);
            URI uri = URI.create(uriString);
            requestBuilder.uri(uri);
        }

        requestBuilder.GET();
        HttpRequest request = requestBuilder.build();
        String responseBody;
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new Exception("Exchange response was not OK: " + response.body());
            }
            responseBody = response.body();
        } catch (Exception e) {
            logger.error("Failed to exchange access token", e);
            return;
        }

        JsonNode root = mapper.readTree(responseBody);
        accessToken.setValue(root.get("access_token").asString());

        feedConfig.getAccessToken().setLongLived(true);
        logger.debug("Successfully exchanged access token for long-lived one");
    }

    /**
     * Refreshes the provided access token.
     * Intentionally synchronous as it either occurs during critical initialization or in
     * a scheduled, OneFeed thread pool managed task.
     *
     * @param feed the feed whose access token to refresh
     */
    public void refreshAccessToken(InstagramFeed feed) {
        logger.debug("Attempting to refresh access token...");
        IgFeedConfig feedConfig = feed.getConfig();
        AccessToken accessToken = feed.getConfig().getAccessToken();

        Builder requestBuilder = HttpRequest.newBuilder();
        String clientSecret = URLEncoder.encode(providerConfig.getAppSecret(), StandardCharsets.UTF_8);
        String accessTokenStr = URLEncoder.encode(feedConfig.getAccessToken().getValue(),
            StandardCharsets.UTF_8);

        // Hit the correct endpoint with the correct args
        if (feedConfig.getLoginType() == LoginType.FACEBOOK) {
            String clientId = URLEncoder.encode(providerConfig.getAppId(), StandardCharsets.UTF_8);

            String uriString = String.format("%s/oauth/access_token?grant_type=fb_exchange_token&client_id=%s&client_secret=%s&fb_exchange_token=%s",
                getBaseUrl(feedConfig.getLoginType()), clientId, clientSecret, accessTokenStr);

            URI uri = URI.create(uriString);
            requestBuilder.uri(uri);
        } else { // Insta login
            String uriString = String.format("%s/refresh_access_token?grant_type=ig_refresh_token&access_token=%s",
                feedConfig.getLoginType(), accessTokenStr);
            URI uri = URI.create(uriString);
            requestBuilder.uri(uri);
        }

        requestBuilder.GET();
        HttpRequest request = requestBuilder.build();
        String responseBody;
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new Exception("Refresh response was not OK: " + response.body());
            }
            responseBody = response.body();
        } catch (Exception e) {
            logger.error("Failed to refresh access token", e);
            return;
        }

        // The "refreshed" token is really just a new one, there's no true refresh unlike insta
        if (feedConfig.getLoginType() == LoginType.FACEBOOK) {
            JsonNode root = mapper.readTree(responseBody);
            accessToken.setValue(root.get("access_token").asString());
        }

        logger.debug("Successfully refreshed access token");
    }

    /**
     * Gets the base API URL for the platform depending on the {@code loginType}.
     * @param loginType the type of login used for the Meta app
     * @return the base API URL corresponding to the provided {@code loginType}
     */
    private String getBaseUrl(LoginType loginType) {
        return ((loginType == LoginType.FACEBOOK) ? "https://graph.facebook.com/" : "https://graph.instagram.com/") + API_VERSION;
    }
}
