package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.api.content.Normalizer;
import dev.jqb.onefeed.api.impl.OneFeedContent;
import dev.jqb.onefeed.instagramplugin.apimodel.InstagramContent;

/**
 * A normalizer for {@link InstagramContent} --> {@link OneFeedContent}
 */
public class InstagramContentNormalizer implements Normalizer<InstagramContent, OneFeedContent> {
    private boolean useTotalMetricsForNormalization;

    public InstagramContentNormalizer(boolean useTotalMetricsForNormalization) {
        this.useTotalMetricsForNormalization = useTotalMetricsForNormalization;
    }

    @Override
    public OneFeedContent normalize(InstagramContent content) {
        return null;
    }
}
