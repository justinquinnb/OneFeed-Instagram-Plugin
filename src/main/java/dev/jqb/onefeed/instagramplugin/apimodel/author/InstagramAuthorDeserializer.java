package dev.jqb.onefeed.instagramplugin.apimodel.author;

import dev.jqb.onefeed.api.feed.SourceInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * A custom {@link InstagramAuthor} deserializer from Instagram profile API responses
 */
public class InstagramAuthorDeserializer extends StdDeserializer<InstagramAuthor> {

    public InstagramAuthorDeserializer() {
        super(InstagramAuthor.class);
    }

    @Override
    public InstagramAuthor deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode root = p.readValueAsTree();
        String id = root.get("id").asString();
        String handle = root.get("username").asString();
        String feedUrl = String.format("https://instagram.com/%s", handle);

        SourceInfo source = new SourceInfo(null, null, id, feedUrl);
        InstagramAuthor author = new InstagramAuthor(source, handle);

        author.setProfilePictureUrl(root.get("profile_picture_url").asString());
        author.setName(root.get("name").asString());

        // Extra fields when not in lite mode
        author.setBiography(root.path("biography").asString(null));
        author.setWebsite(root.path("website").asString(null));
        author.setFollowersCount(root.path("followers_count").asInt(0));
        author.setMediaCount(root.path("media_count").asInt(0));

        return author;
    }
}
