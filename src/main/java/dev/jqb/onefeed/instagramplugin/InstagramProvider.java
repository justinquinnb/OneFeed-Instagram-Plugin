package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.core.actor.ActorTransformer;
import dev.jqb.onefeed.core.actor.OneFeedActor;
import dev.jqb.onefeed.core.content.ContentTransformer;
import dev.jqb.onefeed.core.content.OneFeedContent;
import dev.jqb.onefeed.core.feed.Feed;
import dev.jqb.onefeed.core.feed.FeedUpdate;
import dev.jqb.onefeed.core.platform.Platform;
import dev.jqb.onefeed.core.provider.Provider;
import dev.jqb.onefeed.core.provider.WebhookEnabledProvider;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthor;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthorNormalizer;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContent;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContentNormalizer;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A provider of Instagram content
 */
public class InstagramProvider extends Provider<InstagramContent, InstagramAuthor> implements
    WebhookEnabledProvider {

    /**
     * The handler used to actually make the API requests
     */
    private final RequestHandler requestHandler;
    private final ContentTransformer<InstagramContent, OneFeedContent> contentNormalizer;
    private final ActorTransformer<InstagramAuthor, OneFeedActor> authorNormalizer;
    private List<InstagramFeed> feeds;

    /**
     * Constructs a new {@code InstagramProvider}
     *
     * @param id the unique ID assigned to the provider
     * @param requestHandler the handler used to actually make the API requests
     * @param useLiteMode whether to request only the bare minimum fields to complete
     *                    {@link OneFeedActor} and {@link OneFeedContent} objects from the API
     * @param useTotalMetricsForNormalization whether to use "total" style metrics for content
     *
     * @see <a href="https://developers.facebook.com/docs/instagram-platform/reference/instagram-media/">Instagram API Docs</a>
     */
    public InstagramProvider(String id, RequestHandler requestHandler,
        boolean useLiteMode, boolean useTotalMetricsForNormalization
    ) {
        super(id);
        this.requestHandler = requestHandler;
        this.contentNormalizer = new InstagramContentNormalizer(
            !useLiteMode && useTotalMetricsForNormalization);
        this.authorNormalizer = new InstagramAuthorNormalizer();
    }

    @Override
    public List<Feed> getFeeds() {
        return List.of();
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
    public Mono<InstagramAuthor> fetchAuthor(String feedName) {
        return requestHandler.fetchAuthor(feedName);
    }

    @Override
    public FeedUpdate<InstagramContent, InstagramAuthor> handleWebhookNotif(String notifPayload) {
        return null; // TODO implement
    }
}
