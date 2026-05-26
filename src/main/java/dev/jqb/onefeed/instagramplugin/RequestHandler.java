package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.api.content.ContentFilter;
import dev.jqb.onefeed.api.feed.FilteredContent;
import dev.jqb.onefeed.api.feed.Profile;
import dev.jqb.onefeed.instagramplugin.apimodel.InstagramContent;
import dev.jqb.onefeed.instagramplugin.apimodel.PageResult;
import dev.jqb.onefeed.instagramplugin.apimodel.ProfileDeserializer;
import dev.jqb.onefeed.instagramplugin.config.AccessToken;
import dev.jqb.onefeed.instagramplugin.config.FeedConfig;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Handles all requests to the Instagram API
 */
public class RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final String API_VERSION = "v25.0";

    /**
     * A mapping of feed names to Instagram User IDs.<br><br>
     *
     * Importantly, User IDs are Meta App-specific. That is, the User ID of a single account will
     * differ depending on the Meta App associated with the access token used.
     */
    private ConcurrentHashMap<String, String> userIds = new ConcurrentHashMap<>();

    private final HttpClient httpClient = HttpClient.newBuilder()
        .followRedirects(Redirect.NORMAL)
        .build();

    private ObjectMapper mapper;

    /**
     * The plugin's environment variables
     */
    private final HashMap<String, FeedConfig> feedEnvs;

    /**
     * Creates a new, initialized {@code RequestHandler} using the given {@code providerEnv}.
     * @param feedEnvs the environment variables for each feed
     * @return a new {@code RequestHandler} already initialized and ready for use
     */
    public static RequestHandler using(HashMap<String, FeedConfig> feedEnvs) {
        RequestHandler requestHandler = new RequestHandler(feedEnvs);
        requestHandler.init();

        return requestHandler;
    }

    /**
     * Constructs a new {@code RequestHandler}.
     * @param feedEnvs the environment variables for each feed
     */
    private RequestHandler(HashMap<String, FeedConfig> feedEnvs) {
        this.feedEnvs = feedEnvs;
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Profile.class, new ProfileDeserializer(Profile.class));
        this.mapper = JsonMapper.builder().addModule(module).build();
    }

    /**
     * Initializes the request handler.
     */
    public void init() {
        for (String feedName : feedEnvs.keySet()) {
            FeedConfig feedConfig = feedEnvs.get(feedName);
            AccessToken accessTokenInfo = feedConfig.getAccessToken();

            // Get the access tokens ready
            if (!accessTokenInfo.isLongLived() && accessTokenInfo.isExchangeForLongLived()) {
                exchangeAccessToken(feedConfig);
            } else if (accessTokenInfo.isLongLived() && accessTokenInfo.isAutoRefresh()){
                refreshAccessToken(feedConfig);
            }

            // Retrieve the ID of each user
            userIds.put(feedName, fetchInstaUserId(feedName));
        }
    }

    /**
     * Fetches the given {@code amount} of most recently published content from {@code this}
     * provider's content source for the given feed.
     *
     * @param feedName the name of the feed whose content to retrieve
     * @param amount the target amount of content to retrieve
     * @param filters the filters to try applying if supported by the API or best performed on the
     *                content itself
     * @param config a map of configuration options for this specific request
     *
     * @return a {@link Mono} that emits a {@link FilteredContent} package containing the retrieved content
     */
    public Mono<FilteredContent<InstagramContent>> fetchRecentContent(String feedName, int amount,
        List<ContentFilter<?>> filters, HashMap<String, String> config
    ) {
        return fetchContentPage(feedName, amount, null, filters, config).map(pageResult -> {

        });
    }

    /**
     * Fetches the given {@code amount} of most recently published content from {@code this}
     * provider's content source for the given feed.
     *
     * @param feedName the name of the feed whose content to retrieve
     * @param amount the target amount of content to retrieve
     * @param cursor a cursor of format {@code <cursor>-<offset>}
     * @param filters the filters to try applying if supported by the API or best performed on the
     *                content itself
     * @param config a map of configuration options for this specific request
     *
     * @return a {@link Mono} that emits a {@link FilteredContent} package containing the retrieved content
     */
    public Mono<FilteredContent<InstagramContent>> fetchRecentContent(String feedName, int amount,
        String cursor, List<ContentFilter<?>> filters, HashMap<String, String> config
    ) {
        String[] cursorParts = cursor.split("-");
        String cursorPart = cursorParts[0];
        int offsetPart = Integer.parseInt(cursorParts[1]);
        Mono<PageResult> firstPage = fetchContentPage(feedName, amount, cursorPart, filters, config);

        firstPage.expand(pageResult -> {

        })

        return null;
    }

    /**
     *
     * @param feedName the name of the feed whose content to retrieve
     * @param amount the target amount of content to retrieve
     * @param after the cursor of the page to retrieve
     * @param filters the filters to try applying if supported by the API or best performed on the
     *                content itself
     * @param config a map of configuration options for this specific request
     *
     * @return a {@link Mono} that emits a {@link FilteredContent} package containing the retrieved content
     */
    private Mono<PageResult> fetchContentPage(String feedName, int amount, String after,
        List<ContentFilter<?>> filters, HashMap<String, String> config
    ) {
        URI uri = getContentPageUri(feedName, amount, after, filters, config);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        return Mono.fromCompletionStage(httpClient.sendAsync(request, BodyHandlers.ofString()))
            .map(response ->
                mapper.readValue(response.body(), PageResult.class));
    }

    /**
     * Gets the URI to fetch the desired content from.
     *
     * @param feedName the name of the feed whose content to retrieve
     * @param amount the target amount of content to retrieve (max 10)
     * @param after the cursor to start fetching content after, or null if the first page is desired
     * @param filters the filters to try applying if supported by the API or best performed on the
     *                content itself
     * @param config a map of configuration options for this specific request
     *
     * @return the URI to fetch the desired content from
     */
    private URI getContentPageUri(String feedName, int amount, String after,
        List<ContentFilter<?>> filters, HashMap<String, String> config
    ) {
        AccessToken accessTokenInfo = feedEnvs.get(feedName).getAccessToken();
        String baseUrl = getBaseUrl(feedEnvs.get(feedName).getLoginType());

        String afterArg;
        if (after != null)
            afterArg = "&after=" + after;
        else {
            afterArg = "";
        }

        return URI.create(String.format(
                "%s/%s/media?fields=alt_text,media_type,media_url,like_count,caption,timestamp," +
                "permalink,children{media_url,media_type}&access_token=%s&limit=%s%s",
                baseUrl, userIds.get(feedName), accessTokenInfo.getValue(), Math.min(amount, 10),
                afterArg
            )
        );
    }

    /**
     * Fetches the profile of the author responsible for the content in feed {@code feedName}.
     *
     * @param feedName the name of the feed whose corresponding profile to retrieve
     * @return a {@link Mono} that emits the {@link Profile} of the author of the desired feed
     */
    public Mono<Profile> fetchProfile(String feedName) {
        FeedConfig feedConfig = feedEnvs.get(feedName);

        // Possible because the endpoint for Insta login is just "me", else it's the specific ID,
        // where both paths have the same args
        String userId = (feedConfig.getLoginType() == LoginType.FACEBOOK)
            ? userIds.get(feedName)
            : "me";

        URI uri = URI.create(
            String.format("%s/%s?access_token=%s&fields=id,name,username,profile_picture_url",
                getBaseUrl(feedConfig.getLoginType()), userId,
                feedConfig.getAccessToken().getValue()
            )
        );
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        return Mono.fromCompletionStage(httpClient.sendAsync(request, BodyHandlers.ofString()))
            .map(response -> mapper.readValue(response.body(), Profile.class));
    }

    /**
     * Provides the Instagram User ID for the provided {@code feedName}.
     *
     * @param feedName the name of the feed whose Instagram User ID to retrieve
     *
     * @return a {@link Mono} emitting the name of the Instagram User ID corresponding to the
     * {@code feedName}, sourced either from the cache {@link #userIds} or from the API itself
     */
    private String fetchInstaUserId(String feedName) {
        FeedConfig feedConfig = feedEnvs.get(feedName);

        // Get the correct URI based on the login type
        URI uri;
        if (feedConfig.getLoginType() == LoginType.FACEBOOK) {
            logger.debug("Fetching Instagram User ID from Facebook Login for Business feed: {}", feedName);
            uri = URI.create(String.format("%s/me/accounts?access_token=%s&fields=instagram_business_account",
                getBaseUrl(feedConfig.getLoginType()), feedConfig.getAccessToken().getValue()));

        } else { // Insta login
            logger.debug("Fetching Instagram User ID from Business Login for Instagram feed: {}", feedName);
            uri = URI.create(String.format("%s/me?access_token=%s",
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
        if (feedConfig.getLoginType() == LoginType.FACEBOOK) {
            return root.get("data").asArray().get(0).get("instagram_business_account").get("id").asString();
        } else { // Insta login
            return root.get("id").asString();
        }
    }

    /**
     * Exchanges the access token for a long-lived one.
     * Intentionally synchronous as it's critical initialization.
     *
     * @param feedConfig the feed environment to whose access token to exchange for a long-lived one
     */
    private void exchangeAccessToken(FeedConfig feedConfig) {
        logger.debug("Attempting to exchange access token for long-lived one...");
        AccessToken accessToken = feedConfig.getAccessToken();

        Builder requestBuilder = HttpRequest.newBuilder();
        String clientSecret = URLEncoder.encode(feedConfig.getAppSecret(), StandardCharsets.UTF_8);
        String accessTokenStr = URLEncoder.encode(feedConfig.getAccessToken().getValue(),
            StandardCharsets.UTF_8);

        // Hit the correct endpoint with the correct args
        if (feedConfig.getLoginType() == LoginType.FACEBOOK) {
            String clientId = URLEncoder.encode(feedConfig.getAppId(), StandardCharsets.UTF_8);

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
     * @param feedConfig the feed environment whose access token to refresh
     */
    private void refreshAccessToken(FeedConfig feedConfig) {
        logger.debug("Attempting to refresh access token...");
        AccessToken accessToken = feedConfig.getAccessToken();

        Builder requestBuilder = HttpRequest.newBuilder();
        String clientSecret = URLEncoder.encode(feedConfig.getAppSecret(), StandardCharsets.UTF_8);
        String accessTokenStr = URLEncoder.encode(feedConfig.getAccessToken().getValue(),
            StandardCharsets.UTF_8);

        // Hit the correct endpoint with the correct args
        if (feedConfig.getLoginType() == LoginType.FACEBOOK) {
            String clientId = URLEncoder.encode(feedConfig.getAppId(), StandardCharsets.UTF_8);

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
     * Refreshes the access tokens of all feeds that indicated they should be auto-refreshed.
     * Intentionally synchronous as it's executed by the OneFeed thread pool handler.
     */
    public void refreshAllAccessTokens() {
        for (String feedName : feedEnvs.keySet()) {
            FeedConfig feedConfig = feedEnvs.get(feedName);
            if (feedConfig.getAccessToken().isAutoRefresh() && feedConfig.getAccessToken().isLongLived()) {
                refreshAccessToken(feedConfig);
            }
        }
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
