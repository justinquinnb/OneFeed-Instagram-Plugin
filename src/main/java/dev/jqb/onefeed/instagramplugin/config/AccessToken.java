package dev.jqb.onefeed.instagramplugin.config;

import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

/**
 * A Meta API access token
 */
@Getter
@Setter
public class AccessToken {

    /**
     * The access token for the targeted profile
     */
    private String value;

    /**
     * Whether the access token is long-lived
     */
    private boolean isLongLived = true;

    /**
     * Whether the access token should be exchanged for a long-lived one
     */
    private boolean exchangeForLongLived = false;

    /**
     * Whether the access token should be automatically refreshed
     */
    private boolean autoRefresh = true;

    /**
     * Constructs a new {@code ApiCredentials} object.
     *
     * @param value the access token for the targeted profile
     */
    public AccessToken(String value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code AccessToken} object from the untyped, raw env feed data provided by
     * OneFeed.
     *
     * @param rawAccessTokenData the raw access token data provided by OneFeed
     */
    public AccessToken(HashMap<String, String> rawAccessTokenData) {
        this.value = rawAccessTokenData.get("value");
        this.isLongLived = Boolean.parseBoolean(rawAccessTokenData.get("isLongLived"));
        this.exchangeForLongLived = Boolean
            .parseBoolean(rawAccessTokenData.get("exchangeForLongLived"));
        this.autoRefresh = Boolean.parseBoolean(rawAccessTokenData.get("autoRefresh"));
    }
}
