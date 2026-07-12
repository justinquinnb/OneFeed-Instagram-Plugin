package dev.jqb.onefeed.instagramplugin;

import dev.jqb.onefeed.core.feed.Feed;
import dev.jqb.onefeed.core.feed.FeedCursor;
import dev.jqb.onefeed.core.feed.FeedId;
import dev.jqb.onefeed.instagramplugin.apimodel.author.BasicIgUserInfo;
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
    private BasicIgUserInfo author;

    private RequestHandler requestHandler;

    /**
     * Creates a new {@code InstagramFeed} with the provided configuration. Must be initialized
     * using {@link #init()} prior to use.
     *
     * @param feedId the ID to assign to the feed
     * @param url the URL where the feed can be accessed on its source platform
     * @param config the configuration for the feed
     * @param requestHandler the request handler to use for making API requests
     *
     * @see #using
     */
    public InstagramFeed(FeedId feedId, String url, IgFeedConfig config, RequestHandler requestHandler) {
        super(feedId, url);
        this.config = config;
        this.requestHandler = requestHandler;
    }

    /**
     * Creates a new {@code InstagramFeed} with the provided configuration and initializes it
     */
    public static InstagramFeed using(FeedId feedId, IgFeedConfig config, RequestHandler requestHandler) {
        InstagramFeed feed = new InstagramFeed(feedId, null, config, requestHandler);
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
        this.author = requestHandler.fetchInstaUserInfo(this);
        this.url = "https://instagram.com/" + this.author.getUsername();
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
