package dev.jqb.onefeed.instagramplugin.apimodel.content;

import dev.jqb.onefeed.core.actor.Actor;
import dev.jqb.onefeed.core.content.Content;
import dev.jqb.onefeed.core.feed.FeedId;
import dev.jqb.onefeed.core.platform.ExternalRef;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthor;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramCollaborator;
import java.time.Instant;
import java.util.ArrayList;
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
public class InstagramContent extends Content {
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

    // The the first child equals the primary media of this content
    // I.e. the media contained in this object == children.getFirst()
    // Just a quirk of the Instagram API
    private List<InstagramContentChild> children;

    private String authorId;
    private List<InstagramCollaborator> collaborators;

    public InstagramContent(
        FeedId feedId,
        ExternalRef externalRef,
        @Nullable String nextPageCursor,
        Instant published,
        String authorId,
        List<InstagramCollaborator> collaborators
    ) {
        if (collaborators == null) {
            collaborators = List.of();
        }
        ArrayList<String> authors = new ArrayList<>(collaborators.size() + 1);
        authors.add(authorId);
        String collabPostIdPrefix = String.format("(%s)", externalRef.id());
        authors.addAll(
            collaborators.stream().map(c -> collabPostIdPrefix + c.getExternalRef().id()).toList());
        super(feedId, externalRef, nextPageCursor, published, authors);
    }

    /**
     * Gets the primary media of {@code this} content object (i.e. the solo photo or video) as a
     * {@link InstagramContentChild}.
     *
     * @return the primary media of {@code this} content object (i.e. the solo photo or video) as a
     * {@link InstagramContentChild}
     */
    public InstagramContentChild getPrimaryMediaAsChild() {
        return new InstagramContentChild(mediaType, mediaUrl, externalRef.id(),
            altText, thumbnailUrl);
    }
}
