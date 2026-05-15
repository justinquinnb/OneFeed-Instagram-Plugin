package dev.jqb.onefeed.instagramplugin.config;

import dev.jqb.onefeed.api.feed.ProviderConfig;
import java.util.HashMap;
import lombok.Getter;

/**
 * A test environment for an Instagram provider, holding just instantiated {@link FeedConfig}s
 */
@Getter
public class InstagramTestEnv extends ProviderConfig {
    private HashMap<String, FeedConfig> feedEnvs;

    public InstagramTestEnv(HashMap<String, Object> providerVars, HashMap<String, FeedConfig> feedEnvs) {
        super(providerVars, null);
        this.feedEnvs = feedEnvs;
    }

}
