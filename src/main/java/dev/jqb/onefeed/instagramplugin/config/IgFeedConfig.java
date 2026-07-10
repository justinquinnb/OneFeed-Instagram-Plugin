package dev.jqb.onefeed.instagramplugin.config;

import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single Instagram feed's environment/configuration
 */
@Getter
@Setter
public class IgFeedConfig {

    private static final Logger logger = LoggerFactory.getLogger(IgFeedConfig.class);

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
    public IgFeedConfig(HashMap<String, Object> rawFeedEnvData) {
        logger.trace("Parsing feed config...");
        this.loginType = LoginType.valueOf(((String) rawFeedEnvData.get("loginType")).toUpperCase());
        this.accessToken = new AccessToken((HashMap<String, String>)rawFeedEnvData.get("accessToken"));
    }

    /**
     * Constructs a new {@code FeedEnv} object from the given arguments.
     *
     * @param loginType the type of login required to access the feed's data
     * @param accessToken the access token used to access the feed's data
     */
    public IgFeedConfig(LoginType loginType, AccessToken accessToken) {
        this.loginType = loginType;
        this.accessToken = accessToken;
    }
}
