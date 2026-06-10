package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.api.provider.OneFeedProviderPlugin;
import dev.jqb.onefeed.api.provider.ProviderConfig;
import dev.jqb.onefeed.api.plugin.FixedDelayTask;
import dev.jqb.onefeed.api.plugin.ScheduledTask;
import dev.jqb.onefeed.api.plugin.ScheduledTasks;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthor;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContent;
import dev.jqb.onefeed.instagramplugin.config.FeedConfig;
import dev.jqb.onefeed.instagramplugin.config.InstagramTestEnv;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A OneFeed plugin that provides Instagram feed content
 */
public class InstagramPlugin extends OneFeedProviderPlugin implements ScheduledTasks
{
    private static final Logger logger = LoggerFactory.getLogger(InstagramPlugin.class);

    private InstagramProvider provider;
    private HashMap<String, FeedConfig> feedEnvs;
    private RequestHandler requestHandler;

    /**
     * Constructs a new {@code InstagramPlugin} within a provided {@code ProviderConfig}
     * @param providerConfig the {@code InstagramPlugin}-specific configuration containing API
     *                  keys, etc.
     */
    public InstagramPlugin(String pluginId, ProviderConfig providerConfig) {
        super(pluginId, providerConfig);
        this.feedEnvs = parseFeedConfigs(providerConfig);
    }

    public InstagramPlugin(String pluginId, InstagramTestEnv providerConfig) {
        super(pluginId, providerConfig);
        this.feedEnvs = providerConfig.getFeedEnvs();
    }

    @Override
    public void start() {
        boolean useTotalMetricsForNormalization = Boolean.parseBoolean(
            (String)providerConfig.getPluginVars().getOrDefault(
                "useTotalMetricsForNormalization", "false"));

        this.requestHandler = RequestHandler.using(pluginId, feedEnvs,
            providerConfig.isUsingLiteFetchMode(), useTotalMetricsForNormalization);

        this.provider = new InstagramProvider(requestHandler,
            providerConfig.isUsingLiteFetchMode(), useTotalMetricsForNormalization);

        logger.info("Instagram plugin started");
    }

    @Override
    public List<ScheduledTask> getScheduledTasks() {
        return List.of(new FixedDelayTask(requestHandler::refreshAllAccessTokens,
            "Refresh access tokens", Duration.ofDays(59)));
    }

    @Override
    public InstagramProvider getProvider() {
        return provider;
    }

    @Override
    public List<Class<?>> getClassesToDeserialize() {
        return List.of(InstagramContent.class, InstagramAuthor.class);
    }

    @Override
    public List<String> getFeedNames() {
        return List.of(feedEnvs.keySet().toArray(new String[0]));
    }

    /**
     * Parses the configuration for each feed into a {@code HashMap} of feed names to
     * {@code FeedEnv} objects.
     *
     * @param providerConfig the configuration the plugin is running with
     *
     * @return the parsed configuration for each feed
     */
    private static HashMap<String, FeedConfig> parseFeedConfigs(ProviderConfig providerConfig) {
        logger.trace("Parsing feed configs...");
        HashMap<String, FeedConfig> feedConfigs = new HashMap<>();

        providerConfig.getFeeds().forEach((feedName, rawFeedEnvData) -> {
            feedConfigs.put(feedName, new FeedConfig(rawFeedEnvData));
        });
        return feedConfigs;
    }
}
