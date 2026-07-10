package dev.jqb.onefeed.instagramplugin.apimodel.author;

import dev.jqb.onefeed.core.actor.Actor;
import dev.jqb.onefeed.core.platform.ExternalRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A collaborator of an Instagram post
 *
 * @see <a href="https://developers.facebook.com/documentation/instagram-platform/instagram-graph-api/reference/ig-media/collaborators">Instagram API Docs</a>
 */
@Getter
@Setter
@ToString(callSuper = true)
public class InstagramCollaborator extends Actor {

    /**
     * Whether the collaborator has accepted the invitation to collaborate on the post
     */
    private String inviteStatus;

    public InstagramCollaborator(
        String providerId, ExternalRef externalRef, String handle, String inviteStatus
    ) {
        super(providerId, externalRef, handle);
        this.inviteStatus = inviteStatus;
    }

    /**
     * Whether the collaborator has accepted the invitation to collaborate on the post
     */
    public boolean hasAccepted() {
        return inviteStatus.equals("Accepted");
    }
}
