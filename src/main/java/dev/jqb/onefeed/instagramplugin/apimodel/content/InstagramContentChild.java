package dev.jqb.onefeed.instagramplugin.apimodel.content;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * A child of {@link InstagramContent}
 */
@Getter
@Setter
@NoArgsConstructor
public class InstagramContentChild {

    /**
     * The URL of the media
     */
    private String mediaUrl;

    /**
     * The type of media (e.g. image, video)
     */
    private MediaType mediaType;

    /**
     * The ID of the media
     */
    private String id;

    /**
     * The alt text of the media
     */
    @Nullable
    private String altText;

    /**
     * The URL to the video's thumbnail, included if the {@link #mediaType} is
     * {@link MediaType#VIDEO}
     */
    @Nullable
    private String thumbnailUrl;

    /**
     * Creates an {@link InstagramContentChild} with the given media type, URL, and ID.
     * @param mediaType the type of media (e.g. image, video)
     * @param mediaUrl the URL of the media
     * @param id the ID of the media
     */
    public InstagramContentChild(MediaType mediaType, String mediaUrl, String id,
        @Nullable String altText, @Nullable String thumbnailUrl
    ) {
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.id = id;
        this.altText = altText;
        this.thumbnailUrl = thumbnailUrl;
    }
}
