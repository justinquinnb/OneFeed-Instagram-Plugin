package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.api.feed.OneFeedProviderPlugin;
import dev.jqb.onefeed.api.feed.ProviderEnv;
import dev.jqb.onefeed.api.plugin.FixedDelayTask;
import dev.jqb.onefeed.api.plugin.ScheduledTask;
import dev.jqb.onefeed.api.plugin.ScheduledTasks;
import dev.jqb.onefeed.instagramplugin.config.FeedEnv;
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
    private HashMap<String, FeedEnv> feedEnvs;
    private RequestHandler requestHandler;

    /**
     * Constructs a new {@code InstagramPlugin} within a provided {@code ProviderEnv}
     * @param providerEnv the {@code InstagramPlugin}-specific environment variables containing API
     *                  keys, etc.
     */
    public InstagramPlugin(ProviderEnv providerEnv) {
        super(providerEnv);
        this.feedEnvs = parseFeedEnvs(providerEnv);
    }

    public InstagramPlugin(InstagramTestEnv providerEnv) {
        super(providerEnv);
        this.feedEnvs = providerEnv.getFeedEnvs();
    }

    @Override
    public void start() {
        this.requestHandler = RequestHandler.using(feedEnvs);
        this.provider = new InstagramProvider(requestHandler);
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
        return List.of(InstagramContent.class);
    }

    /**
     * Parses the environment variables for each feed into a {@code HashMap} of feed names to
     * {@code FeedEnv} objects.
     *
     * @param providerEnv the environment variables the plugin is running with
     *
     * @return the parsed environment variables for each feed
     */
    private static HashMap<String, FeedEnv> parseFeedEnvs(ProviderEnv providerEnv) {
        logger.trace("Parsing feed variables...");
        HashMap<String, FeedEnv> feedEnvs = new HashMap<>();

        providerEnv.getFeeds().forEach((feedName, rawFeedEnvData) -> {
            feedEnvs.put(feedName, new FeedEnv(rawFeedEnvData));
        });
        return feedEnvs;
    }
}
