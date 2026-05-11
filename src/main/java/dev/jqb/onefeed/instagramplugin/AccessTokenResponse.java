package dev.jqb.onefeed.instagramplugin;

/**
 * The response from the Instagram or Facebook Graph API after a token exchange or refresh
 */
public class AccessTokenResponse {
    public String access_token;
    public String token_type;
    public int expires_in;
}
