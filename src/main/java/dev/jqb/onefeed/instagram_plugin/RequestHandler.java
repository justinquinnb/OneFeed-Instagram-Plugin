package dev.jqb.onefeed.instagram_plugin;

import dev.jqb.onefeed.api.pipeline.ContentFilter;
import dev.jqb.onefeed.api.pipeline.ProviderResponse;
import dev.jqb.onefeed.instagram_plugin.config.AccessToken;
import dev.jqb.onefeed.instagram_plugin.config.FeedEnv;
import java.util.HashMap;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Handles all requests to the Instagram API
 */
public class RequestHandler {

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
            AccessToken accessTokenInfo = feedEnvs.get(feedName).getAccessToken();

            if (accessTokenInfo.isLongLived() && accessTokenInfo.isExchangeForLongLived()) {
                exchangeAccessToken(accessTokenInfo.getValue());
            } else if (accessTokenInfo.isLongLived() && accessTokenInfo.isAutoRefresh()){
                refreshAccessToken(accessTokenInfo.getValue());
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
     * @return a {@link Mono} that emits a {@link ProviderResponse} containing the retrieved content
     */
    public Mono<ProviderResponse<InstagramContent>> fetchRecentContent(String feedName, int amount,
        List<ContentFilter<?>> filters, HashMap<String, String> config
    ) {
        AccessToken accessTokenInfo = feedEnvs.get(feedName).getAccessToken();
//        HttpRequest request = HttpRequest.newBuilder()
//            .uri(domain.resolve())

        return null;
    }

    /**
     * Exchanges the access token for a long-lived one.
     *
     * @param accessToken the access token to exchange
     */
    private void exchangeAccessToken(String accessToken) {

    }

    /**
     * Refreshes the provided access token.
     *
     * @param accessToken the access token to refresh
     */
    private void refreshAccessToken(String accessToken) {

    }

    /**
     * Resolves the user IDs for all feeds, placing the results in the {@code feedEnvs} map.
     */
    private void fetchUserIds(RequestHandler requestHandler) {

    }
}
