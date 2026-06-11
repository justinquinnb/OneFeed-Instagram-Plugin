package dev.jqb.onefeed.instagramplugin.apimodel.content;

import dev.jqb.onefeed.core.content.PlatformContent;
import dev.jqb.onefeed.core.feed.SourceInfo;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

/**
 * A piece of content as it comes straight from Instagram's API
 *
 * @see <a href="https://developers.facebook.com/docs/instagram-platform/reference/instagram-media/">Instagram API Docs</a>
 */
@Getter
@Setter
@ToString(callSuper = true)
public class InstagramContent extends PlatformContent {
    private MediaType mediaType;
    private String mediaUrl;
    private String caption;

    @Nullable
    private String altText;

    @Nullable
    private String thumbnailUrl;

    // Distinction specified by API docs
    private int likeCount;
    private int totalLikeCount;

    private int sharesCount;
    private int savedCount;
    private int repostsCount;

    // Distinction specified by API docs
    private int commentsCount;
    private int totalCommentsCount;

    // Distinction specified by API docs
    private int viewCount;
    private int totalViewsCount;

    private List<InstagramContentChild> children;

    public InstagramContent(SourceInfo source, @Nullable String nextPageCursor, Instant published) {
        super(source, nextPageCursor, published);
    }

    /**
     * Gets the primary media of {@code this} content object (i.e. the solo photo or video) as a
     * {@link InstagramContentChild}.
     *
     * @return the primary media of {@code this} content object (i.e. the solo photo or video) as a
     * {@link InstagramContentChild}
     */
    public InstagramContentChild getPrimaryMediaAsChild() {
        return new InstagramContentChild(mediaType, mediaUrl, source.getIdOnPlatform(),
            altText, thumbnailUrl);
    }
}
