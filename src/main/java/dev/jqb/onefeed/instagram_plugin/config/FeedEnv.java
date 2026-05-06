package dev.jqb.onefeed.instagram_plugin.config;

import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

/**
 * A single Instagram feed's environment/configuration
 */
@Getter
@Setter
public class FeedEnv {

    /**
     * The type of login required to access the feed's data
     */
    private LoginType loginType;

    /**
     * The access token used to access the feed's data
     */
    private AccessToken accessToken;

    /**
     * Constructs a new {@code FeedEnv} object from the untyped, raw env feed data provided by
     * OneFeed.
     *
     * @param rawFeedEnvData the raw feed env data provided by OneFeed
     */
    @SuppressWarnings("unchecked")
    public FeedEnv(HashMap<String, Object> rawFeedEnvData) {
        this.loginType = LoginType.valueOf(((String) rawFeedEnvData.get("loginType")).toUpperCase());
        this.accessToken = new AccessToken((HashMap<String, String>)rawFeedEnvData.get("accessToken"));
    }
}
