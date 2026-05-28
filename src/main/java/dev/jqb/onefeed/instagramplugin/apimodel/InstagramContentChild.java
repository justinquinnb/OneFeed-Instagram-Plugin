package dev.jqb.onefeed.instagramplugin.apimodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A child of {@link InstagramContent}
 */
@Getter
@Setter
@NoArgsConstructor
public class InstagramContentChild {
    private String mediaUrl;
    private MediaType mediaType;
    private String id;
}
