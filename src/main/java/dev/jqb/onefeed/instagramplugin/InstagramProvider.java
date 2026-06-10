package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.api.author.AuthorNormalizer;
import dev.jqb.onefeed.api.content.ContentNormalizer;
import dev.jqb.onefeed.api.content.PlatformCursor;
import dev.jqb.onefeed.api.provider.AutoProvider;
import dev.jqb.onefeed.api.feed.FeedUpdate;
import dev.jqb.onefeed.api.provider.Platform;
import dev.jqb.onefeed.api.impl.OneFeedContent;
import dev.jqb.onefeed.api.impl.OneFeedAuthor;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthor;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthorNormalizer;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContent;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContentNormalizer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A provider of Instagram content
 */
public class InstagramProvider implements AutoProvider<InstagramContent, InstagramAuthor> {

    /**
     * The handler used to actually make the API requests
     */
    private final RequestHandler requestHandler;
    private final ContentNormalizer<InstagramContent, OneFeedContent> contentNormalizer;
    private final AuthorNormalizer<InstagramAuthor, OneFeedAuthor> authorNormalizer;

    /**
     * Constructs a new {@code InstagramProvider}
     * @param requestHandler the handler used to actually make the API requests
     */
    public InstagramProvider(RequestHandler requestHandler, boolean useTotalMetricsForNormalization) {
        this.requestHandler = requestHandler;
        this.contentNormalizer = new InstagramContentNormalizer(useTotalMetricsForNormalization);
        this.authorNormalizer = new InstagramAuthorNormalizer();
    }

    @Override
    public Flux<InstagramContent> fetchRecentContent(String feedName, int amount) {
        return requestHandler.fetchRecentContent(feedName, amount);
    }

    @Override
    public Flux<InstagramContent> fetchRecentContent(String feedName, int amount,
        PlatformCursor cursor
    ) {
        return requestHandler.fetchRecentContent(feedName, amount, cursor);
    }

    @Override
    public ContentNormalizer<InstagramContent, OneFeedContent> getContentNormalizer() {
        return contentNormalizer;
    }

    @Override
    public AuthorNormalizer<InstagramAuthor, OneFeedAuthor> getAuthorNormalizer() {
        return authorNormalizer;
    }

    @Override
    public Platform getPlatformInfo() {
        return new Platform("Instagram", "https://www.instagram.com/");
    }

    @Override
    public Mono<InstagramAuthor> fetchAuthor(String feedName) {
        return requestHandler.fetchAuthor(feedName);
    }

    @Override
    public FeedUpdate<InstagramContent, InstagramAuthor> handleWebhookNotif(String notifPayload) {
        return null;
    }
}
