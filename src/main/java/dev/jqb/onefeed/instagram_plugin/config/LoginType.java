package dev.jqb.onefeed.instagram_plugin.config;

/**
 * The type of login used when giving your Meta application access to your Instagram account.
 *
 * @see <a href="https://developers.facebook.com/docs/instagram-platform/">Instagram Platform Login Types Overview</a>
 */
public enum LoginType {
    /**
     * Facebook Login. Referred to by Meta as "Facebook Login for Business" or "Instagram API with
     * Facebook Login."
     *
     * @see <a href="https://developers.facebook.com/docs/instagram-platform/instagram-api-with-facebook-login">Instagram API with Facebook Login Docs</a>
     */
    FACEBOOK,

    /**
     * Instagram Login. Referred to by Meta as "Business Login for Instagram" or "Instagram API with
     * Facebook Login."
     *
     * @see <a href="https://developers.facebook.com/docs/instagram-platform/instagram-api-with-instagram-login">Instagram API with Instagram Login Docs</a>
     */
    INSTAGRAM
}
