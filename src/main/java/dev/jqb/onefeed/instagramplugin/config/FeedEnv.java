package dev.jqb.onefeed.instagramplugin.config;

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
     * The app ID of your Facebook Login for Business app. Sometimes called client ID by Meta's
     * docs
     */
    private String appId;

    /**
     * The app secret of your Meta app. Sometimes called app ID by Meta's docs.
     */
    private String appSecret;

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
        this.appId = (String) rawFeedEnvData.get("appId");
        this.appSecret = (String) rawFeedEnvData.get("appSecret");
    }
}
