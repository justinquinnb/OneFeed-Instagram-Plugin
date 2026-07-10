package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.core.plugin.FixedDelayTask;
import dev.jqb.onefeed.core.plugin.ScheduledTask;
import dev.jqb.onefeed.core.plugin.ScheduledTasks;
import dev.jqb.onefeed.core.provider.OneFeedProviderPlugin;
import dev.jqb.onefeed.core.provider.Provider;
import dev.jqb.onefeed.core.provider.ProviderConfig;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthor;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramCollaborator;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContent;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContentChild;
import dev.jqb.onefeed.instagramplugin.apimodel.content.MediaType;
import dev.jqb.onefeed.instagramplugin.config.AccessToken;
import dev.jqb.onefeed.instagramplugin.config.IgProviderConfig;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A OneFeed plugin that provides Instagram feed content
 */
public class InstagramPlugin extends OneFeedProviderPlugin implements ScheduledTasks {
    private static final Logger logger = LoggerFactory.getLogger(InstagramPlugin.class);

    private InstagramProvider provider;
    private RequestHandler requestHandler;

    /**
     * Constructs a new {@code InstagramPlugin} within a provided {@code ProviderConfig}
     * @param providerConfig the {@code InstagramPlugin}-specific configuration containing API
     *                  keys, etc.
     */
    public InstagramPlugin(String pluginId, ProviderConfig providerConfig) {
        super(pluginId, providerConfig);
    }

    @Override
    public void start() {
        IgProviderConfig igProviderConfig = new IgProviderConfig(providerConfig);

        this.requestHandler = new RequestHandler(pluginId, igProviderConfig);
        this.provider = new InstagramProvider(pluginId, igProviderConfig, requestHandler);
        this.provider.init();

        logger.info("Instagram plugin started");
    }

    @Override
    public List<ScheduledTask> getScheduledTasks() {
        return List.of(new FixedDelayTask(this::refreshAllAccessTokens,
            "Refresh access tokens", Duration.ofDays(59)));
    }

    @Override
    public Provider<InstagramContent, InstagramAuthor> getProvider() {
        return provider;
    }

    @Override
    public List<Class<?>> getClassesToDeserialize() {
        return List.of(InstagramContent.class, InstagramContentChild.class, MediaType.class,
            InstagramAuthor.class, InstagramCollaborator.class);
    }

    /**
     * Refreshes all long-lived access tokens for all feeds who have auto-refresh enabled
     */
    private void refreshAllAccessTokens() {
        for (InstagramFeed feed : provider.getFeeds()) {
            AccessToken accessToken = feed.getConfig().getAccessToken();
            if (accessToken.isAutoRefresh() && accessToken.isLongLived()) {
                feed.refreshAccessToken();
            }
        }
    }
}
