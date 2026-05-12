package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.api.content.ContentFilter;
import dev.jqb.onefeed.api.feed.FilteredContent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

/**
 * Handles all requests to the Instagram API
 */
public class RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private HashMap<String, String> userIds = new HashMap<>();

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

            // Now that the access token's ready, resolve the user ID for each feed
            userIds.put(feedName, resolveUserId(feedEnv));
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
            String baseUrl = "https://graph.facebook.com/oauth/access_token";
            String clientId = URLEncoder.encode(feedEnv.getAppId(), StandardCharsets.UTF_8);

            String uriString = String.format("%s?grant_type=fb_exchange_token&client_id=%s&client_secret=%s&fb_exchange_token=%s",
                baseUrl, clientId, clientSecret, accessTokenStr);

            URI uri = URI.create(uriString);
            requestBuilder.uri(uri);
        } else {
            String baseUrl = "https://graph.instagram.com/access_token";
            String uriString = String.format("%s?grant_type=ig_exchange_token&client_secret=%s&access_token=%s",
                baseUrl, clientSecret, accessTokenStr);
            URI uri = URI.create(uriString);
            requestBuilder.uri(uri);
        }

        requestBuilder.GET();
        HttpRequest request = requestBuilder.build();
        String responseBody = "";
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new Exception("Exchange response was not OK: " + response.body());
            }
            responseBody = response.body();
        } catch (Exception e) {
            logger.error("Failed to exchange access token", e);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        AccessTokenResponse accessTokenResponse = mapper.readValue(responseBody, AccessTokenResponse.class);

        accessToken.setValue(accessTokenResponse.access_token);
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
            String baseUrl = "https://graph.facebook.com/oauth/access_token";
            String clientId = URLEncoder.encode(feedEnv.getAppId(), StandardCharsets.UTF_8);

            String uriString = String.format("%s?grant_type=fb_exchange_token&client_id=%s&client_secret=%s&fb_exchange_token=%s",
                baseUrl, clientId, clientSecret, accessTokenStr);

            URI uri = URI.create(uriString);
            requestBuilder.uri(uri);
        } else {
            String baseUrl = "https://graph.instagram.com/refresh_access_token";
            String uriString = String.format("%s?grant_type=ig_refresh_token&access_token=%s",
                baseUrl, accessTokenStr);
            URI uri = URI.create(uriString);
            requestBuilder.uri(uri);
        }

        requestBuilder.GET();
        HttpRequest request = requestBuilder.build();
        String responseBody = "";
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
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
            ObjectMapper mapper = new ObjectMapper();
            AccessTokenResponse accessTokenResponse = mapper.readValue(responseBody, AccessTokenResponse.class);

            accessToken.setValue(accessTokenResponse.access_token);
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
     * Resolves the user IDs for all feeds, placing the results in the {@code feedEnvs} map.
     *
     * @param feedEnv the feed environment whose user ID to resolve
     */
    private String resolveUserId(FeedEnv feedEnv) {


        return null;
    }
}
