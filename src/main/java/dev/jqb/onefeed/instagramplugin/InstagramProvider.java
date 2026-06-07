package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.api.content.Normalizer;
import dev.jqb.onefeed.api.content.PlatformCursor;
import dev.jqb.onefeed.api.feed.AutoProvider;
import dev.jqb.onefeed.api.feed.FeedUpdate;
import dev.jqb.onefeed.api.feed.Platform;
import dev.jqb.onefeed.api.impl.OneFeedContent;
import dev.jqb.onefeed.api.impl.Profile;
import dev.jqb.onefeed.instagramplugin.apimodel.InstagramContent;
import java.util.HashMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A provider of Instagram content
 */
public class InstagramProvider implements AutoProvider<InstagramContent> {

    /**
     * The handler used to actually make the API requests
     */
    private final RequestHandler requestHandler;
    private final Normalizer<InstagramContent, OneFeedContent> normalizer;

    /**
     * Constructs a new {@code InstagramProvider}
     * @param requestHandler the handler used to actually make the API requests
     */
    public InstagramProvider(RequestHandler requestHandler, boolean useTotalMetricsForNormalization) {
        this.requestHandler = requestHandler;
        this.normalizer = new InstagramContentNormalizer(useTotalMetricsForNormalization);
    }

    @Override
    public Flux<InstagramContent> fetchRecentContent(String feedName, int amount) {
        return requestHandler.fetchRecentContent(feedName, amount);
    }

    @Override
    public Flux<InstagramContent> fetchRecentContent(String feedName, int amount,
        PlatformCursor cursor
    ) {
        return requestHandler.fetchRecentContent(feedName, amount, cursor);
    }

    @Override
    public Normalizer<InstagramContent, OneFeedContent> getNormalizer() {
        return normalizer;
    }

    @Override
    public Platform getPlatformInfo() {
        return new Platform("Instagram", "https://www.instagram.com/");
    }

    @Override
    public Mono<Profile> getProfile(String feedName) {
        return requestHandler.fetchProfile(feedName);
    }

    @Override
    public FeedUpdate<InstagramContent> handleWebhookNotif(String notifPayload) {
        return null;
    }
}
