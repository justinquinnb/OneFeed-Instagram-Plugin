package dev.jqb.onefeed.instagramplugin.apimodel;

import dev.jqb.onefeed.api.feed.SourceInfo;
import dev.jqb.onefeed.api.impl.Profile;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * A custom {@link Profile} deserializer from Instagram profile API responses
 */
public class ProfileDeserializer extends StdDeserializer<Profile> {

    public ProfileDeserializer() {
        super(Profile.class);
    }

    @Override
    public Profile deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode root = p.readValueAsTree();
        String id = root.get("id").asString();
        String name = root.get("name").asString();
        String handle = root.get("username").asString();
        String profilePicSrc = root.get("profile_picture_url").asString();
        String feedUrl = String.format("https://instagram.com/%s", handle);

        SourceInfo source = new SourceInfo(null, id, feedUrl);

        return new Profile(source, handle, name, profilePicSrc);
    }
}
