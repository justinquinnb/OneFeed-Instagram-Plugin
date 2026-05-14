package dev.jqb.onefeed.instagramplugin.config;

import dev.jqb.onefeed.api.feed.ProviderEnv;
import java.util.HashMap;
import lombok.Getter;

/**
 *
 */
@Getter
public class InstagramTestEnv extends ProviderEnv {
    private HashMap<String, FeedEnv> feedEnvs;

    public InstagramTestEnv(HashMap<String, Object> providerVars, HashMap<String, FeedEnv> feedEnvs) {
        super(providerVars, null);
        this.feedEnvs = feedEnvs;
    }

}
