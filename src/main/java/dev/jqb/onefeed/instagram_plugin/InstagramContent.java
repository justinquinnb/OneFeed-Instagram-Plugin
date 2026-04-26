package dev.jqb.onefeed.instagram_plugin;

import dev.jqb.onefeed.api.model.data.RawContent;
import dev.jqb.onefeed.api.model.data.SourceInfo;
import java.time.Instant;

/**
 * A piece of content as it comes straight from Instagram's API
 */
public class InstagramContent extends RawContent {
    private Instant published;
    private SourceInfo source;

    @Override
    public Instant getPublished() {
        return published;
    }

    @Override
    public SourceInfo getSource() {
        return source;
    }
}
