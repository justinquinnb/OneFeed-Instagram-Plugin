package dev.jqb.onefeed.instagramplugin.apimodel.author;

import dev.jqb.onefeed.core.platform.ExternalRef;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * A custom {@link InstagramCollaborator} deserializer from Instagram Media Collaborators API responses
 */
public class InstagramCollaboratorDeserializer extends StdDeserializer<InstagramCollaborator> {
    private String providerId;

    public InstagramCollaboratorDeserializer(String providerId) {
        super(InstagramCollaborator.class);
        this.providerId = providerId;
    }

    @Override
    public InstagramCollaborator deserialize(
        JsonParser p, DeserializationContext ctx) throws JacksonException {
        JsonNode root = p.readValueAsTree();
        String id = root.get("id").asString();
        String handle = root.get("username").asString();
        String inviteStatus = root.get("invite_status").asString();
        String authorUrl = String.format("https://instagram.com/%s", handle);

        ExternalRef externalRef = new ExternalRef(authorUrl, id);

        return new InstagramCollaborator(providerId, externalRef, handle, inviteStatus);
    }
}
