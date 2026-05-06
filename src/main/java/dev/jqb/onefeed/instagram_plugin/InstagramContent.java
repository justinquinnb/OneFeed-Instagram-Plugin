package dev.jqb.onefeed.instagram_plugin;

import dev.jqb.onefeed.api.content.RawContent;
import dev.jqb.onefeed.api.feed.Profile;
import dev.jqb.onefeed.api.feed.SourceInfo;
import java.time.Instant;

/**
 * A piece of content as it comes straight from Instagram's API
 */
public class InstagramContent extends RawContent {
    private Instant published;
    private SourceInfo<Profile> source;

    @Override
    public Instant getPublished() {
        return published;
    }

    @Override
    public SourceInfo<Profile> getSource() {
        return source;
    }
}
