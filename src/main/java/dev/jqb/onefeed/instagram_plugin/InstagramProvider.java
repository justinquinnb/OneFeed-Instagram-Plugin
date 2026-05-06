package dev.jqb.onefeed.instagram_plugin;

import dev.jqb.onefeed.api.content.ContentPackage;
import dev.jqb.onefeed.api.feed.Platform;
import dev.jqb.onefeed.api.impl.OneFeedContent;
import dev.jqb.onefeed.api.pipeline.AutoProvider;
import dev.jqb.onefeed.api.pipeline.ContentFilter;
import dev.jqb.onefeed.api.pipeline.Normalizer;
import dev.jqb.onefeed.api.pipeline.ProviderResponse;
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
    public Mono<ProviderResponse<InstagramContent>> fetchRecentContent(String feedName, int amount,
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
    public String getWebhookSlug() {
        return "/instagram";
    }

    @Override
    public ContentPackage<InstagramContent> getUpdatedContent(String notifPayload) {
        return null;
    }
}
