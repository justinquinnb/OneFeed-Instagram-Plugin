package dev.jqb.onefeed.instagramplugin.apimodel.content;

import dev.jqb.onefeed.core.content.ContentTransformer;
import dev.jqb.onefeed.core.content.OneFeedContent;
import dev.jqb.onefeed.core.content.OneFeedMedia;
import dev.jqb.onefeed.core.platform.ExternalRef;
import jakarta.activation.MimeType;
import java.util.ArrayList;
import java.util.List;

/**
 * A normalizer for {@link InstagramContent} --> {@link OneFeedContent}
 */
public class InstagramContentNormalizer implements
    ContentTransformer<InstagramContent, OneFeedContent> {

    /**
     * Whether to use the total metrics for normalization (e.g. total_likes instead of likes).
     *
     * @see <a href="https://developers.facebook.com/docs/instagram-platform/reference/instagram-media/">Instagram API - Media</a>
     */
    private boolean useTotalMetricsForNormalization;

    public InstagramContentNormalizer(boolean useTotalMetricsForNormalization) {
        this.useTotalMetricsForNormalization = useTotalMetricsForNormalization;
    }

    @Override
    public OneFeedContent transform(InstagramContent content) {
        OneFeedContent ofc = new OneFeedContent();

        ofc.setPublished(content.getPublished());
        ofc.setFeedId(content.getFeedId());
        ofc.setExternalRef(content.getExternalRef());
        ofc.setNextPageCursor(content.getNextPageCursor().get());

        ofc.setBody(content.getCaption());
        if (useTotalMetricsForNormalization) {
            ofc.setPrimaryReactionCount(content.getTotalLikeCount());
        } else {
            ofc.setPrimaryReactionCount(content.getLikeCount());
        }

        List<InstagramContentChild> children = new ArrayList<>();
        if (content.getMediaType() == MediaType.CAROUSEL_ALBUM) {
            children.addAll(content.getChildren());
        } else {
            children.add(content.getPrimaryMediaAsChild());
        }

        ofc.setMedia(convertToMedia(content.getExternalRef().url(), children));

        return ofc;
    }

    /**
     * Converts the provided {@code children} into a list of {@link OneFeedMedia} objects, preserving the
     * original order.
     *
     * @param postLink the link to the Instagram post, which is used to derive the link to each
     *                 {@link OneFeedMedia} object in the context of the post
     * @param children the {@link InstagramContentChild}s to convert
     *
     * @return a list of {@link OneFeedMedia} objects representing the {@code children}, preserving their
     * original order
     */
    public static List<OneFeedMedia> convertToMedia(String postLink, List<InstagramContentChild> children) {
        List<OneFeedMedia> media = new ArrayList<>();

        int i = 1;
        for (InstagramContentChild child : children) {
            MimeType ofMediaType = (child.getMediaType() == MediaType.VIDEO) ?
                MimeType.IMAGE : MimeType.VIDEO; // TODO read docs to figure out
            String mediaUrl = postLink + "?img_index=" + i; // It's img_index even for videos

            OneFeedMedia mediaItem = new OneFeedMedia(ofMediaType, mediaUrl);
            mediaItem.setAltText(child.getAltText());
            mediaItem.setThumbnailSrc(child.getThumbnailUrl());
            mediaItem.setSrc(child.getMediaUrl());
            media.add(mediaItem);
            i++;
        }

        return media;
    }
}
