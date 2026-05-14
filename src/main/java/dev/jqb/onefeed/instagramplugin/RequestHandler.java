package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.api.content.ContentFilter;
import dev.jqb.onefeed.api.feed.FilteredContent;
import dev.jqb.onefeed.api.feed.Profile;
import dev.jqb.onefeed.instagramplugin.config.AccessToken;
import dev.jqb.onefeed.instagramplugin.config.FeedEnv;
import dev.jqb.onefeed.instagramplugin.config.LoginType;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
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

    /**
     * A mapping of feed names to Instagram User IDs.<br><br>
     *
     * Importantly, User IDs are Meta App-specific. That is, the User ID of a single account will
     * differ depending on the Meta App associated with the access token used.
     */
    private ConcurrentHashMap<String, String> userIds = new ConcurrentHashMap<>();

    private final HttpClient httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private ObjectMapper mapper;

    /**
     * The plugin's environment variables
     */
    private final HashMap<String, FeedEnv> feedEnvs;

    /**
     * Creates a new, initialized {@code RequestHandler} using the given {@code providerEnv}.
     * @param feedEnvs the environment variables for each feed
     * @return a new {@code RequestHandler} already initialized and ready for use
     */
    public static RequestHandler using(HashMap<String, FeedEnv> feedEnvs) {
        RequestHandler requestHandler = new RequestHandler(feedEnvs);
        requestHandler.init();

        return requestHandler;
    }

    /**
     * Constructs a new {@code RequestHandler}.
     * @param feedEnvs the environment variables for each feed
     */
    private RequestHandler(HashMap<String, FeedEnv> feedEnvs) {
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
            FeedEnv feedEnv = feedEnvs.get(feedName);
            AccessToken accessTokenInfo = feedEnv.getAccessToken();

            // Get the access tokens ready
            if (!accessTokenInfo.isLongLived() && accessTokenInfo.isExchangeForLongLived()) {
                exchangeAccessToken(feedEnv);
            } else if (accessTokenInfo.isLongLived() && accessTokenInfo.isAutoRefresh()){
                refreshAccessToken(feedEnv);
            }
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
        AccessToken accessTokenInfo = feedEnvs.get(feedName).getAccessToken();
//        HttpRequest request = HttpRequest.newBuilder()
//            .uri(domain.resolve())

        return null;
    }

    /**
     * Fetches the profile of the author responsible for the content in feed {@code feedName}.
     *
     * @param feedName the name of the feed whose corresponding profile to retrieve
     * @return a {@link Mono} that emits the {@link Profile} of the author of the desired feed
     */
    public Mono<Profile> fetchProfile(String feedName) {
        FeedEnv feedEnv = feedEnvs.get(feedName);

        // Possible because the endpoint for Insta login is just "me", else it's the specific ID,
        // where both paths have the same args
        Mono<String> userIdMono = (feedEnv.getLoginType() == LoginType.FACEBOOK)
            ? getOrFetchInstaUserId(feedName)
            : Mono.just("me");

        return userIdMono
            .flatMap(userId -> {
                URI uri = URI.create(
                    String.format("%s/%s?access_token=%s&fields=id,name,username,profile_picture_url",
                        getBaseUrl(feedEnv.getLoginType()), userId,
                        feedEnv.getAccessToken().getValue()
                    )
                );
                HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

                return Mono.fromFuture(httpClient.sendAsync(request, BodyHandlers.ofString()));
            })
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
    public Mono<String> getOrFetchInstaUserId(String feedName) {
        FeedEnv feedEnv = feedEnvs.get(feedName);

        // Just provide the ID from the hashmap if we already have it
        if (userIds.containsKey(feedName)) {
            return Mono.just(userIds.get(feedName));
        }

        // Otherwise go get it for the first time
        if (feedEnv.getLoginType() == LoginType.FACEBOOK) {
            return fetchInstaIdFromFbLogin(feedName);
        }

        return fetchInstaIdFromInstaLogin(feedName);
    }

    /**
     * Fetches the Instagram User ID for the provided feed using the Instagram login approach.
     *
     * @param feedName the name of the feed whose Instagram User ID to retrieve
     * @return a {@link Mono} emitting the name of the Instagram User ID corresponding to the
     * {@code feedName}
     */
    private Mono<String> fetchInstaIdFromInstaLogin(String feedName) {
        logger.debug("Fetching Instagram User ID from Business Login for Instagram feed: {}", feedName);
        FeedEnv feedEnv = feedEnvs.get(feedName);
        URI uri = URI.create(String.format("%s/me?access_token=%s",
            getBaseUrl(feedEnv.getLoginType()), feedEnv.getAccessToken().getValue()));
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        return Mono
            .fromFuture(httpClient.sendAsync(request, BodyHandlers.ofString()))
            .map(response -> {
                JsonNode root = mapper.readTree(response.body());
                return root.get("id").asString();
            })
            .doOnNext(id -> userIds.put(feedName, id));
    }

    /**
     * Fetches the Instagram User ID for the provided feed using the Facebook login approach.
     *
     * @param feedName the name of the feed whose Instagram User ID to retrieve
     * @return a mono emitting the name of the Instagram User ID corresponding to the
     * {@code feedName}
     */
    private Mono<String> fetchInstaIdFromFbLogin(String feedName) {
        logger.debug("Fetching Instagram User ID from Facebook Login for Business feed: {}", feedName);
        FeedEnv feedEnv = feedEnvs.get(feedName);
        URI uri = URI.create(String.format("%s/me/accounts?access_token=%s&fields=instagram_business_account",
            getBaseUrl(feedEnv.getLoginType()), feedEnv.getAccessToken().getValue()));
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        return Mono
            .fromFuture(httpClient.sendAsync(request, BodyHandlers.ofString()))
            .map(response -> {
                JsonNode root = mapper.readTree(response.body());
                return root.get("data").asArray().get(0).get("instagram_business_account").get("id").asString();
            })
            .doOnNext(id -> userIds.put(feedName, id));
    }

    /**
     * Exchanges the access token for a long-lived one.
     * Intentionally synchronous as it's critical initialization.
     *
     * @param feedEnv the feed environment to whose access token to exchange for a long-lived one
     */
    private void exchangeAccessToken(FeedEnv feedEnv) {
        logger.debug("Attempting to exchange access token for long-lived one...");
        AccessToken accessToken = feedEnv.getAccessToken();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        String clientSecret = URLEncoder.encode(feedEnv.getAppSecret(), StandardCharsets.UTF_8);
        String accessTokenStr = URLEncoder.encode(feedEnv.getAccessToken().getValue(),
            StandardCharsets.UTF_8);

        // Hit the correct endpoint with the correct args
        if (feedEnv.getLoginType() == LoginType.FACEBOOK) {
            String clientId = URLEncoder.encode(feedEnv.getAppId(), StandardCharsets.UTF_8);

            String uriString = String.format("%s/oauth/access_token?grant_type=fb_exchange_token&client_id=%s&client_secret=%s&fb_exchange_token=%s",
                getBaseUrl(feedEnv.getLoginType()), clientId, clientSecret, accessTokenStr);

            URI uri = URI.create(uriString);
            requestBuilder.uri(uri);
        } else {
            String uriString = String.format("%s/access_token?grant_type=ig_exchange_token&client_secret=%s&access_token=%s",
                getBaseUrl(feedEnv.getLoginType()), clientSecret, accessTokenStr);
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

        feedEnv.getAccessToken().setLongLived(true);
        logger.debug("Successfully exchanged access token for long-lived one");
    }

    /**
     * Refreshes the provided access token.
     * Intentionally synchronous as it either occurs during critical initialization or in
     * a scheduled, OneFeed thread pool managed task.
     *
     * @param feedEnv the feed environment whose access token to refresh
     */
    private void refreshAccessToken(FeedEnv feedEnv) {
        logger.debug("Attempting to refresh access token...");
        AccessToken accessToken = feedEnv.getAccessToken();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        String clientSecret = URLEncoder.encode(feedEnv.getAppSecret(), StandardCharsets.UTF_8);
        String accessTokenStr = URLEncoder.encode(feedEnv.getAccessToken().getValue(),
            StandardCharsets.UTF_8);

        // Hit the correct endpoint with the correct args
        if (feedEnv.getLoginType() == LoginType.FACEBOOK) {
            String clientId = URLEncoder.encode(feedEnv.getAppId(), StandardCharsets.UTF_8);

            String uriString = String.format("%s/oauth/access_token?grant_type=fb_exchange_token&client_id=%s&client_secret=%s&fb_exchange_token=%s",
                getBaseUrl(feedEnv.getLoginType()), clientId, clientSecret, accessTokenStr);

            URI uri = URI.create(uriString);
            requestBuilder.uri(uri);
        } else {
            String uriString = String.format("%s/refresh_access_token?grant_type=ig_refresh_token&access_token=%s",
                feedEnv.getLoginType(), accessTokenStr);
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
        if (feedEnv.getLoginType() == LoginType.FACEBOOK) {
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
            FeedEnv feedEnv = feedEnvs.get(feedName);
            if (feedEnv.getAccessToken().isAutoRefresh() && feedEnv.getAccessToken().isLongLived()) {
                refreshAccessToken(feedEnv);
            }
        }
    }

    /**
     * Gets the base API URL for the platform depending on the {@code loginType}.
     * @param loginType the type of login used for the Meta app
     * @return the base API URL corresponding to the provided {@code loginType}
     */
    private String getBaseUrl(LoginType loginType) {
        return (loginType == LoginType.FACEBOOK) ? "https://graph.facebook.com" : "https://graph.instagram.com";
    }
}
