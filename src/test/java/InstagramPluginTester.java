import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.jqb.onefeed.api.feed.Profile;
import dev.jqb.onefeed.api.feed.Provider;
import dev.jqb.onefeed.instagramplugin.InstagramPlugin;
import dev.jqb.onefeed.instagramplugin.config.AccessToken;
import dev.jqb.onefeed.instagramplugin.config.FeedEnv;
import dev.jqb.onefeed.instagramplugin.config.InstagramTestEnv;
import dev.jqb.onefeed.instagramplugin.config.LoginType;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * A test class for the provider
 */
@Slf4j
public class InstagramPluginTester {
    private static InstagramPlugin plugin;
    private static Provider provider;
    private static String feedName;

    @BeforeAll
    public static void createPlugin() {
        // Read the .env
        Dotenv dotEnv = Dotenv.load();

        // Create the plugin env from it
        HashMap<String, FeedEnv> feedEnvs = new HashMap<>();

        LoginType loginType = LoginType.valueOf(((String) dotEnv.get("LOGIN_TYPE")).toUpperCase());
        String accessTokenValue = dotEnv.get("ACCESS_TOKEN");
        boolean isLongLived = Boolean.parseBoolean(dotEnv.get("TOKEN_LONG_LIVED"));
        boolean exchangeForLongLived = Boolean.parseBoolean(dotEnv.get("TOKEN_EXCHANGE_FOR_LONG_LIVED"));
        boolean autoRefresh = Boolean.parseBoolean(dotEnv.get("TOKEN_AUTO_REFRESH"));
        AccessToken accessToken = new AccessToken(accessTokenValue, isLongLived, exchangeForLongLived, autoRefresh);

        String appId = dotEnv.get("APP_ID");
        String appSecret = dotEnv.get("APP_SECRET");

        FeedEnv feedEnv = new FeedEnv(loginType, accessToken, appId, appSecret);
        feedName = dotEnv.get("FEED_NAME");
        feedEnvs.put(feedName, feedEnv);

        plugin = new InstagramPlugin(new InstagramTestEnv(null, feedEnvs));
        plugin.start();
        provider = plugin.getProvider();
    }

    @Test
    public void getProfile() {
        Mono<Profile> mono = provider.getProfile(feedName);

        StepVerifier.create(mono)
            .assertNext(profile -> {
                assertNotNull(profile);

                assertNotNull(profile.getProfilePicSrc());
                assertFalse(profile.getProfilePicSrc().isBlank());

                assertNotNull(profile.getFeedUrl());
                assertFalse(profile.getFeedUrl().isBlank());

                assertNotNull(profile.getHandle());
                assertFalse(profile.getHandle().isBlank());

                assertNotNull(profile.getId());
                assertFalse(profile.getId().isBlank());

                assertNotNull(profile.getName());
                assertFalse(profile.getName().isBlank());

                log.info(profile.toString());
            })
            .verifyComplete();
    }
}
