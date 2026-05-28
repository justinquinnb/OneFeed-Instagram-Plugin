package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.api.content.ContentFilter;
import dev.jqb.onefeed.api.content.ContentPackage;
import dev.jqb.onefeed.api.content.Normalizer;
import dev.jqb.onefeed.api.feed.AutoProvider;
import dev.jqb.onefeed.api.feed.FilteredContent;
import dev.jqb.onefeed.api.feed.Platform;
import dev.jqb.onefeed.api.feed.Profile;
import dev.jqb.onefeed.api.impl.OneFeedContent;
import dev.jqb.onefeed.instagramplugin.apimodel.InstagramContent;
import java.util.HashMap;
import java.util.List;
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
    public Mono<FilteredContent<InstagramContent>> fetchRecentContent(String feedName, int amount,
        List<ContentFilter<?>> filters, HashMap<String, String> config) {
        return requestHandler.fetchRecentContent(feedName, amount, filters, config);
    }

    @Override
    public Mono<FilteredContent<InstagramContent>> fetchRecentContent(String feedName, int amount,
        String after, List<ContentFilter<?>> filters, HashMap<String, String> config) {
        return requestHandler.fetchRecentContent(feedName, amount, after, filters, config);
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
    public ContentPackage<InstagramContent> handleWebhookNotif(String notifPayload) {
        return null;
    }
}
