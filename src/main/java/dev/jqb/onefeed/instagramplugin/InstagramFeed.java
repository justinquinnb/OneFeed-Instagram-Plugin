package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.core.feed.Feed;
import dev.jqb.onefeed.core.feed.FeedCursor;
import dev.jqb.onefeed.core.feed.FeedId;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthor;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContent;
import dev.jqb.onefeed.instagramplugin.config.AccessToken;
import dev.jqb.onefeed.instagramplugin.config.IgFeedConfig;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A feed of Instagram posts from a single user's profile
 */
public class InstagramFeed extends Feed<InstagramContent> {

    private static final Logger logger = LoggerFactory.getLogger(InstagramFeed.class);

    @Getter
    private final IgFeedConfig config;

    @Getter
    private String authorId;

    private RequestHandler requestHandler;

    public InstagramFeed(FeedId feedId, IgFeedConfig config, RequestHandler requestHandler) {
        super(feedId);
        this.config = config;
        this.requestHandler = requestHandler;
    }

    /**
     * Creates a new {@code InstagramFeed} with the provided configuration and initializes it
     */
    public static InstagramFeed using(FeedId feedId, IgFeedConfig config, RequestHandler requestHandler) {
        InstagramFeed feed = new InstagramFeed(feedId, config, requestHandler);
        feed.init();
        return feed;
    }

    /**
     * Initializes the feed by fetching the author and performing any necessary token
     * refreshing/exchanges
     */
    public void init() {
        AccessToken accessTokenInfo = config.getAccessToken();

        // Get the access tokens ready
        if (!accessTokenInfo.isLongLived() && accessTokenInfo.isExchangeForLongLived()) {
            requestHandler.exchangeAccessToken(this);
        } else if (accessTokenInfo.isLongLived() && accessTokenInfo.isAutoRefresh()){
            requestHandler.refreshAccessToken(this);
        }

        // Retrieve the author of the feed
        this.authorId = requestHandler.fetchInstaUserId(this);
    }

    @Override
    public Flux<InstagramContent> fetchRecentContent(int amount) {
        return requestHandler.fetchRecentContent(this, amount);
    }

    @Override
    public Flux<InstagramContent> fetchRecentContent(int amount, FeedCursor cursor) {
        return requestHandler.fetchRecentContent(this, amount, cursor);
    }

    /**
     * Fetches the author of this feed.
     */
    public Mono<InstagramAuthor> fetchAuthor() {
        return requestHandler.fetchAuthor(this);
    }

    /**
     * Refreshes the access token of the feed.
     */
    public void refreshAccessToken() {
        requestHandler.refreshAccessToken(this);
    }
}
