package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.api.content.Normalizer;
import dev.jqb.onefeed.api.impl.Media;
import dev.jqb.onefeed.api.impl.OneFeedContent;
import dev.jqb.onefeed.instagramplugin.apimodel.InstagramContent;
import dev.jqb.onefeed.instagramplugin.apimodel.InstagramContentChild;
import dev.jqb.onefeed.instagramplugin.apimodel.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * A normalizer for {@link InstagramContent} --> {@link OneFeedContent}
 */
public class InstagramContentNormalizer implements Normalizer<InstagramContent, OneFeedContent> {

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
    public OneFeedContent normalize(InstagramContent content) {
        OneFeedContent ofc = new OneFeedContent();

        ofc.setPublished(content.getPublished());
        ofc.setSource(content.getSource());
        ofc.setNextPageCursor(content.getNextPageCursor());

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

        ofc.setMedia(convertToMedia(content.getSource().getUrlOnPlatform(), children));

        return ofc;
    }

    /**
     * Converts the provided {@code children} into a list of {@link Media} objects, preserving the
     * original order.
     *
     * @param postLink the link to the Instagram post, which is used to derive the link to each
     *                 {@link Media} object in the context of the post
     * @param children the {@link InstagramContentChild}s to convert
     *
     * @return a list of {@link Media} objects representing the {@code children}, preserving their
     * original order
     */
    public static List<Media> convertToMedia(String postLink, List<InstagramContentChild> children) {
        List<Media> media = new ArrayList<>();

        int i = 1;
        for (InstagramContentChild child : children) {
            Media.MediaType ofMediaType = (child.getMediaType() == MediaType.VIDEO) ?
                Media.MediaType.VIDEO : Media.MediaType.IMAGE;
            String mediaUrl = postLink + "?img_index=" + i; // It's img even for videos

            Media mediaItem = new Media(ofMediaType, mediaUrl);
            mediaItem.setAltText(child.getAltText());
            mediaItem.setThumbnailSrc(child.getThumbnailUrl());
            mediaItem.setSrc(child.getMediaUrl());
            media.add(mediaItem);
            i++;
        }

        return media;
    }
}
