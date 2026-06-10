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
        author.setBiography(root.get("biography").asString());
        author.setWebsite(root.get("website").asString());
        author.setFollowersCount(root.get("followers_count").asInt());
        author.setMediaCount(root.get("media_count").asInt());

        return author;
    }
}
