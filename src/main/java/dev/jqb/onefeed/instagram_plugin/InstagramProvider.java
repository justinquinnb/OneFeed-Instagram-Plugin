package dev.jqb.onefeed.instagram_plugin;

import dev.jqb.onefeed.api.impl.OneFeedContent;
import dev.jqb.onefeed.api.model.data.ContentPackage;
import dev.jqb.onefeed.api.model.data.Platform;
import dev.jqb.onefeed.api.model.data.ProviderResponse;
import dev.jqb.onefeed.api.model.pipeline.AutoProvider;
import dev.jqb.onefeed.api.model.pipeline.Normalizer;
import java.util.HashMap;
import java.util.List;
import org.pf4j.Plugin;
import reactor.core.publisher.Mono;

/**
 * A provider of Instagram content
 */
public class InstagramProvider implements AutoProvider<InstagramContent> {

    @Override
    public Mono<ProviderResponse<InstagramContent>> getContent(String author, int amount, List list, HashMap config) {
        return null;
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
