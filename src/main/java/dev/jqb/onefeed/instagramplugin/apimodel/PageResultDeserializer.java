package dev.jqb.onefeed.instagramplugin.apimodel;

import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * A custom {@link PageResult} deserializer from Instagram media API page responses
 */
public class PageResultDeserializer extends StdDeserializer<PageResult> {

    public PageResultDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public PageResult deserialize(JsonParser p, DeserializationContext ctxt)
        throws JacksonException
    {
        JsonNode root = p.readValueAsTree();
        List<InstagramContent> content = ctxt.readValue(
            root.get("data"),
            new TypeReference<List<InstagramContent>>() {}
        );
        String pageCursor = root.get("paging").get("cursors").get("after").asString();

        return new PageResult();
    }
}
