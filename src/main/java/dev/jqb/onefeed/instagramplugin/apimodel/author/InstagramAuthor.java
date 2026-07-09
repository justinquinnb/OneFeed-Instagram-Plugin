package dev.jqb.onefeed.instagramplugin.apimodel.author;

import dev.jqb.onefeed.core.actor.Actor;
import dev.jqb.onefeed.core.platform.ExternalRef;
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
public class InstagramAuthor extends Actor {
    private String name;
    private String profilePictureUrl;

    private String biography;
    private int followersCount;
    private int mediaCount;
    private String website;

    public InstagramAuthor(String providerId, ExternalRef externalRef, String username) {
        super(providerId, externalRef, username);
    }
}
