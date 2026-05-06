package dev.jqb.onefeed.instagram_plugin;

import dev.jqb.onefeed.api.pipeline.ScheduledTasks;
import dev.jqb.onefeed.api.plugin.FixedDelayTask;
import dev.jqb.onefeed.api.plugin.OneFeedProviderPlugin;
import dev.jqb.onefeed.api.plugin.ProviderEnv;
import dev.jqb.onefeed.api.plugin.ScheduledTask;
import dev.jqb.onefeed.instagram_plugin.config.FeedEnv;
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

    /**
     * Constructs a new {@code InstagramPlugin} within a provided {@code ProviderEnv}
     * @param providerEnv the {@code InstagramPlugin}-specific environment variables containing API
     *                  keys, etc.
     */
    public InstagramPlugin(ProviderEnv providerEnv) {
        super(providerEnv);
        this.feedEnvs = parseFeedEnvs(providerEnv);
    }

    @Override
    public void start() {
        RequestHandler requestHandler = RequestHandler.using(feedEnvs);
        this.provider = new InstagramProvider(requestHandler);
        logger.info("Instagram plugin started");
    }

    @Override
    public List<ScheduledTask> getScheduledTasks() {
        return List.of(new FixedDelayTask(() -> logger.info("TASK!!!"), "Test task", Duration.ofSeconds(5)));
    }

    @Override
    public InstagramProvider getProvider() {
        return provider;
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
        logger.debug("Parsing feed variables...");
        HashMap<String, FeedEnv> feedEnvs = new HashMap<>();

        providerEnv.getFeeds().forEach((feedName, rawFeedEnvData) -> {
            feedEnvs.put(feedName, new FeedEnv(rawFeedEnvData));
        });
        return feedEnvs;
    }
}
