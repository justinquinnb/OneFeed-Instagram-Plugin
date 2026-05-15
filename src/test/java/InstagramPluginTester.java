import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.jqb.onefeed.api.feed.Profile;
import dev.jqb.onefeed.api.feed.Provider;
import dev.jqb.onefeed.instagramplugin.InstagramPlugin;
import dev.jqb.onefeed.instagramplugin.config.AccessToken;
import dev.jqb.onefeed.instagramplugin.config.FeedConfig;
import dev.jqb.onefeed.instagramplugin.config.InstagramTestEnv;
import dev.jqb.onefeed.instagramplugin.config.LoginType;
import dev.jqb.onefeed.plugintestkit.ProviderPluginTests;
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
public class InstagramPluginTester extends ProviderPluginTests<InstagramPlugin> {
    @Override
    protected InstagramPlugin getInitializedPlugin() {
        // Read the .env
        Dotenv dotEnv = Dotenv.load();

        // Create the plugin env from it
        HashMap<String, FeedConfig> feedEnvs = new HashMap<>();

        LoginType loginType = LoginType.valueOf(((String) dotEnv.get("LOGIN_TYPE")).toUpperCase());
        String accessTokenValue = dotEnv.get("ACCESS_TOKEN");
        boolean isLongLived = Boolean.parseBoolean(dotEnv.get("TOKEN_LONG_LIVED"));
        boolean exchangeForLongLived = Boolean.parseBoolean(dotEnv.get("TOKEN_EXCHANGE_FOR_LONG_LIVED"));
        boolean autoRefresh = Boolean.parseBoolean(dotEnv.get("TOKEN_AUTO_REFRESH"));
        AccessToken accessToken = new AccessToken(accessTokenValue, isLongLived, exchangeForLongLived, autoRefresh);

        String appId = dotEnv.get("APP_ID");
        String appSecret = dotEnv.get("APP_SECRET");

        FeedConfig feedConfig = new FeedConfig(loginType, accessToken, appId, appSecret);
        String feedName = dotEnv.get("FEED_NAME");
        feedEnvs.put(feedName, feedConfig);

        InstagramPlugin instance = new InstagramPlugin(new InstagramTestEnv(null, feedEnvs));
        instance.start();

        return instance;
    }
}
