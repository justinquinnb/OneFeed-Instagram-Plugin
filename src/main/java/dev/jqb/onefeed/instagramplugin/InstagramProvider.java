package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.api.content.ContentFilter;
import dev.jqb.onefeed.api.content.ContentPackage;
import dev.jqb.onefeed.api.content.Normalizer;
import dev.jqb.onefeed.api.feed.AutoProvider;
import dev.jqb.onefeed.api.feed.FilteredContent;
import dev.jqb.onefeed.api.feed.Platform;
import dev.jqb.onefeed.api.feed.Profile;
import dev.jqb.onefeed.api.impl.OneFeedContent;
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

    /**
     * Constructs a new {@code InstagramProvider}
     * @param requestHandler the handler used to actually make the API requests
     */
    public InstagramProvider(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public Mono<FilteredContent<InstagramContent>> fetchRecentContent(String feedName, int amount,
        List<ContentFilter<?>> filters, HashMap<String, String> config) {
        return requestHandler.fetchRecentContent(feedName, amount, filters, config);
    }

    @Override
    public Normalizer<InstagramContent, OneFeedContent> getNormalizer() {
        return null;
    }

    @Override
    public Platform getPlatformInfo() {
        return new Platform("Instagram", "https://www.instagram.com/");
    }

    @Override
    public Mono<Profile> getProfile(String feedName) {
        return null;
    }

    @Override
    public ContentPackage<InstagramContent> handleWebhookNotif(String notifPayload) {
        return null;
    }
}
