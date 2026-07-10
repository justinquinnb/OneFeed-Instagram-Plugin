package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.core.actor.ActorTransformer;
import dev.jqb.onefeed.core.actor.OneFeedActor;
import dev.jqb.onefeed.core.content.ContentTransformer;
import dev.jqb.onefeed.core.content.OneFeedContent;
import dev.jqb.onefeed.core.feed.FeedId;
import dev.jqb.onefeed.core.feed.FeedUpdate;
import dev.jqb.onefeed.core.platform.Platform;
import dev.jqb.onefeed.core.provider.WebhookEnabledProvider;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthor;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthorNormalizer;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContent;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContentNormalizer;
import dev.jqb.onefeed.instagramplugin.config.IgProviderConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * A provider of Instagram content
 */
public class InstagramProvider extends WebhookEnabledProvider<InstagramContent, InstagramAuthor> {

    private static final Logger logger = LoggerFactory.getLogger(InstagramProvider.class);

    /**
     * The handler used to actually make the API requests
     */
    private final RequestHandler requestHandler;
    private final ContentTransformer<InstagramContent, OneFeedContent> contentNormalizer;
    private final ActorTransformer<InstagramAuthor, OneFeedActor> authorNormalizer;
    private final List<InstagramFeed> feeds = new ArrayList<>();
    private ConcurrentHashMap<String, InstagramFeed> userFeeds = new ConcurrentHashMap<>();

    /**
     * Constructs a new {@code InstagramProvider}
     *
     * @param id the unique ID assigned to the provider
     * @param config the configuration the provider is running with
     * @param requestHandler the handler used to actually make the API requests
     *
     * @see <a href="https://developers.facebook.com/docs/instagram-platform/reference/instagram-media/">Instagram API Docs</a>
     */
    public InstagramProvider(String id, IgProviderConfig config, RequestHandler requestHandler) {
        super(id);
        this.requestHandler = requestHandler;
        config.getIgFeedConfigs().forEach((feedName, feedConfig) -> {
            FeedId feedId = new FeedId(id, feedName);
            this.feeds.add(new InstagramFeed(feedId, feedConfig, requestHandler));
        });

        this.contentNormalizer = new InstagramContentNormalizer(
            !config.isUsingLiteFetchMode() &&
                config.shouldUseTotalMetricsForNormalization());
        this.authorNormalizer = new InstagramAuthorNormalizer();
    }

    public void init() {
        this.feeds.forEach(InstagramFeed::init);
    }

    @Override
    public List<InstagramFeed> getFeeds() {
        return feeds;
    }

    @Override
    public ContentTransformer<InstagramContent, OneFeedContent> getContentNormalizer() {
        return contentNormalizer;
    }

    @Override
    public ActorTransformer<InstagramAuthor, OneFeedActor> getActorNormalizer() {
        return authorNormalizer;
    }

    @Override
    public Platform getPlatform() {
        return new Platform(this.id, "Instagram", "https://www.instagram.com/");
    }

    @Override
    public Mono<InstagramAuthor> fetchAuthor(String userId) {
        if (userFeeds.containsKey(userId)) {
            return userFeeds.get(userId).fetchAuthor();
        }

        return Mono.empty(); // Can't just fetch any author from Insta per the API. You need the
        // access token of the user you want to fetch
    }

    @Override
    public FeedUpdate<InstagramContent, InstagramAuthor> handleWebhookNotif(String notifPayload) {
        return null; // TODO implement
    }
}
