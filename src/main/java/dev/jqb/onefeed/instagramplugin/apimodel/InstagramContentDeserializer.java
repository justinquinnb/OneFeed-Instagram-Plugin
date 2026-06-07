package dev.jqb.onefeed.instagramplugin.apimodel;

import dev.jqb.onefeed.api.feed.SourceInfo;
import java.time.Instant;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * A custom {@link InstagramContent} deserializer from Instagram media API page responses
 */
public class InstagramContentDeserializer extends StdDeserializer<InstagramContent> {

    public InstagramContentDeserializer() {
        super(InstagramContent.class);
    }

    @Override
    public InstagramContent deserialize(JsonParser p, DeserializationContext ctxt)
        throws JacksonException
    {
        JsonNode root = p.readValueAsTree();

        String idOnPlatform = root.path("id").asString(null);
        String url = root.path("permalink").asString(null);

        Instant published = null;
        if (root.has("timestamp")) {
            // Because Instants apparently need the colon at the end for the offset and insta
            // doesn't provide it
            String adjTimestamp = root.get("timestamp").asString();
            adjTimestamp = adjTimestamp.substring(0, adjTimestamp.length() - 2) + ":00";
            published = Instant.parse(adjTimestamp);
        }

        // The provider sets the cursor and feedId later...
        SourceInfo sourceInfo = new SourceInfo(null, null, idOnPlatform, url);
        InstagramContent content = new InstagramContent(sourceInfo, null, published);

        // Many of these will be null, but the goal is to capture as much data as we can so
        // users can implement their own normalized types and extract what's needed for them
        content.setMediaType(MediaType.valueOf(root.get("media_type").asString()));
        content.setMediaUrl(root.get("media_url").asString());
        content.setCaption(root.get("caption").asString(null));

        content.setAltText(root.path("alt_text").asString(null));
        content.setThumbnailUrl(root.path("thumbnail_url").asString(null));

        content.setLikeCount(root.path("like_count").asInt(0));
        content.setTotalLikeCount(root.path("total_like_count").asInt(0));
        content.setSharesCount(root.path("shares_count").asInt(0));
        content.setSavedCount(root.path("saved_count").asInt(0));
        content.setRepostsCount(root.path("reposts_count").asInt(0));
        content.setCommentsCount(root.path("comments_count").asInt(0));
        content.setTotalCommentsCount(root.path("total_comments_count").asInt(0));
        content.setViewCount(root.path("views_count").asInt(0));
        content.setTotalViewsCount(root.path("total_views_count").asInt(0));

        if (root.has("children")) {
            content.setChildren(ctxt.readValue(root.path("children")
                    .path("data").traverse(ctxt),
                new TypeReference<List<InstagramContentChild>>() {}));
        }

        return content;
    }
}
