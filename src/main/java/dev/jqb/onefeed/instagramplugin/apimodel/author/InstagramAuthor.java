package dev.jqb.onefeed.instagramplugin.apimodel.author;

import dev.jqb.onefeed.api.author.PlatformAuthor;
import dev.jqb.onefeed.api.feed.SourceInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * An author as it comes straight from Instagram's API
 *
 * @see <a href="https://developers.facebook.com/docs/instagram-platform/instagram-graph-api/reference/ig-user">Instagram API Docs</a>
 */
@Getter
@Setter
@ToString(callSuper = true)
public class InstagramAuthor extends PlatformAuthor {
    private String name;
    private String profilePictureUrl;

    private String biography;
    private int followersCount;
    private int mediaCount;
    private String website;

    public InstagramAuthor(SourceInfo source, String username) {
        super(source, username);
    }
}
