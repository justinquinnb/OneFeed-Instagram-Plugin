import dev.jqb.onefeed.instagramplugin.InstagramPlugin;
import dev.jqb.onefeed.instagramplugin.config.AccessToken;
import dev.jqb.onefeed.instagramplugin.config.FeedConfig;
import dev.jqb.onefeed.instagramplugin.config.InstagramTestEnv;
import dev.jqb.onefeed.instagramplugin.config.LoginType;
import dev.jqb.onefeed.plugintestkit.ProviderPluginTests;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

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

        LoginType loginType = LoginType.valueOf(dotEnv.get("LOGIN_TYPE").toUpperCase());
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

        HashMap<String, Object> providerVars = new HashMap<>();
        providerVars.put("USE_TOTAL_METRICS_FOR_NORMALIZATION", "true");
        InstagramPlugin instance = new InstagramPlugin("test", new InstagramTestEnv(providerVars, feedEnvs));
        instance.start();

        return instance;
    }

    @Override
    protected int getContentPerPageLimit() {
        return 10;
    }
}
