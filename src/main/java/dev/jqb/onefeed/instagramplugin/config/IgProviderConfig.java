package dev.jqb.onefeed.instagramplugin.config;

import dev.jqb.onefeed.core.provider.ProviderConfig;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instagram-specific provider config
 */
@Setter
public class IgProviderConfig {

    private static final Logger logger = LoggerFactory.getLogger(IgProviderConfig.class);

    /**
     * The app ID of your Facebook Login for Business app. Sometimes called client ID by Meta's
     * docs
     */
    @Getter
    private String appId;

    /**
     * The app secret of your Meta app. Sometimes called app ID by Meta's docs
     */
    @Getter
    private String appSecret;

    /**
     * Whether to use the total metrics for normalization (e.g. total_likes instead of likes)
     *
     * @see <a href="https://developers.facebook.com/docs/instagram-platform/reference/instagram-media/">Instagram API - Media</a>
     */
    private boolean useTotalMetricsForNormalization;

    /**
     * Whether to use lite fetch mode
     */
    private boolean useLiteFetchMode;

    /**
     * Feed configurations mapped to their feed names
     */
    @Getter
    private HashMap<String, IgFeedConfig> igFeedConfigs;

    public IgProviderConfig(ProviderConfig providerConfig) {
        logger.trace("Parsing provider config...");
        HashMap<String, Object> rawTopLevelConfig = providerConfig.getTopLevelConfig();
        this(
            (String) rawTopLevelConfig.get("appId"),
            (String) rawTopLevelConfig.get("appSecret"),
            Boolean.parseBoolean(
                (String)providerConfig.getTopLevelConfig().getOrDefault(
                    "useTotalMetricsForNormalization", "false")),
            providerConfig.isUsingLiteFetchMode(),
            parseFeedConfigs(providerConfig)
        );
    }

    public IgProviderConfig(
        String appId, String appSecret, boolean useTotalMetricsForNormalization,
        boolean useLiteFetchMode, HashMap<String, IgFeedConfig> igFeedConfigs
    ) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.useTotalMetricsForNormalization = useTotalMetricsForNormalization;
        this.useLiteFetchMode = useLiteFetchMode;
        this.igFeedConfigs = igFeedConfigs;
    }

    /**
     * Creates a new {@code IgProviderConfig} object with the given app ID and app secret.
     *
     * @param appId the app ID of your Facebook Login for Business app. Sometimes called client ID
     *              by Meta's docs
     * @param appSecret the app secret of your Meta app. Sometimes called app ID by Meta's docs
     * @param useTotalMetricsForNormalization whether to use the total metrics for normalization
     */
    public IgProviderConfig(String appId, String appSecret, boolean useTotalMetricsForNormalization) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.useTotalMetricsForNormalization = useTotalMetricsForNormalization;
    }

    public boolean isUsingLiteFetchMode() {
        return useLiteFetchMode;
    }

    public boolean shouldUseTotalMetricsForNormalization() {
        return useTotalMetricsForNormalization;
    }

    /**
     * Parses the configuration for each feed into a {@code HashMap} of feed names to
     * {@link IgFeedConfig} objects.
     *
     * @param providerConfig the configuration the plugin is running with
     *
     * @return the parsed configuration for each feed
     */
    private static HashMap<String, IgFeedConfig> parseFeedConfigs(ProviderConfig providerConfig) {
        HashMap<String, IgFeedConfig> feedConfigs = new HashMap<>();
        providerConfig.getFeedConfigs().forEach((feedName, rawFeedEnvData) -> {
            feedConfigs.put(feedName, new IgFeedConfig(rawFeedEnvData));
        });
        return feedConfigs;
    }
}
