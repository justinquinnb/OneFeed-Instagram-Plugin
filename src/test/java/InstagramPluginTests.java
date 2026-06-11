import dev.jqb.onefeed.core.feed.SourceInfo;
import dev.jqb.onefeed.core.impl.Media;
import dev.jqb.onefeed.core.impl.OneFeedAuthor;
import dev.jqb.onefeed.core.impl.OneFeedContent;
import dev.jqb.onefeed.instagramplugin.InstagramPlugin;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthor;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContent;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContentChild;
import dev.jqb.onefeed.instagramplugin.apimodel.content.MediaType;
import dev.jqb.onefeed.instagramplugin.config.AccessToken;
import dev.jqb.onefeed.instagramplugin.config.FeedConfig;
import dev.jqb.onefeed.instagramplugin.config.InstagramTestEnv;
import dev.jqb.onefeed.instagramplugin.config.LoginType;
import dev.jqb.onefeed.plugintestkit.ProviderPluginTests;
import io.github.cdimascio.dotenv.Dotenv;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A test class for the provider
 */
public class InstagramPluginTests extends ProviderPluginTests<InstagramPlugin> {
    @Override
    protected InstagramPlugin getInitializedPlugin() {
        // Read the .env
        Dotenv dotEnv = Dotenv.load();

        // Create the plugin env from it
        HashMap<String, FeedConfig> feedEnvs = new HashMap<>();

        LoginType loginType = LoginType.valueOf(dotEnv.get("LOGIN_TYPE").toUpperCase());
        String accessTokenValue = dotEnv.get("ACCESS_TOKEN");
        boolean isLongLived = Boolean.parseBoolean(dotEnv.get("TOKEN_LONG_LIVED"));
        boolean exchangeForLongLived = Boolean.parseBoolean(dotEnv.get("TOKEN_EXCHANGE_FOR_LONG_LIVED"));
        boolean autoRefresh = Boolean.parseBoolean(dotEnv.get("TOKEN_AUTO_REFRESH"));
        AccessToken accessToken = new AccessToken(accessTokenValue, isLongLived, exchangeForLongLived, autoRefresh);

        String appId = dotEnv.get("APP_ID");
        String appSecret = dotEnv.get("APP_SECRET");

        FeedConfig feedConfig = new FeedConfig(loginType, accessToken, appId, appSecret);
        String feedName = dotEnv.get("FEED_NAME");
        feedEnvs.put(feedName, feedConfig);

        HashMap<String, Object> providerVars = new HashMap<>();
        providerVars.put("USE_TOTAL_METRICS_FOR_NORMALIZATION", "true");
        InstagramPlugin instance = new InstagramPlugin("test", new InstagramTestEnv(providerVars, feedEnvs));
        instance.start();

        return instance;
    }

    @Override
    protected int getContentPerPageLimit() {
        return 10;
    }

    @Override
    protected InstagramContent getContentNormalizerInput() {
        SourceInfo source = getSampleSourceInfo();
        InstagramContent content = new InstagramContent(source, "testCursor123",
            Instant.ofEpochMilli(1234567890L));

        content.setLikeCount(123);
        content.setMediaType(MediaType.CAROUSEL_ALBUM);
        content.setMediaUrl("https://www.instagram.com/p/DY7NYWGgBRo");
        content.setCaption("Test caption");
        content.setAltText("Test alt text for media 1");

        List<InstagramContentChild> children = new ArrayList<>();
        InstagramContentChild media1 = new InstagramContentChild(MediaType.IMAGE,
            "https://scontent-atl3-2.cdninstagram.com/v/t51.82787-15/1.jpg",
            "mediaId1", "media1AltText", null);
        children.add(media1);

        InstagramContentChild media2 = new InstagramContentChild(MediaType.VIDEO,
            "https://scontent-atl3-2.cdninstagram.com/v/t51.82787-15/2.mp4",
            "mediaId2", "media2AltText", null);
        children.add(media2);
        content.setChildren(children);

        return content;
    }

    @Override
    protected OneFeedContent getExpectedContentNormalizerOutput() {
        InstagramContent content = getContentNormalizerInput();
        OneFeedContent ofc = new OneFeedContent(content.getSource(), "testCursor123",
            content.getPublished(), content.getCaption());
        ofc.setPrimaryReactionCount(content.getLikeCount());

        List<InstagramContentChild> children = content.getChildren();
        InstagramContentChild child1 = children.get(0);
        Media.MediaType mediaType1 = (child1.getMediaType() == MediaType.VIDEO) ?
            Media.MediaType.VIDEO : Media.MediaType.IMAGE;
        Media media1 = new Media(mediaType1, content.getSource().getUrlOnPlatform() + "?img_index=1");
        media1.setAltText(child1.getAltText());
        media1.setThumbnailSrc(child1.getThumbnailUrl());
        media1.setSrc(child1.getMediaUrl());

        List<Media> mediaList = new ArrayList<>();
        mediaList.add(media1);

        InstagramContentChild child2 = children.get(1);
        Media.MediaType mediaType2 = (child2.getMediaType() == MediaType.VIDEO) ?
            Media.MediaType.VIDEO : Media.MediaType.IMAGE;
        Media media2 = new Media(mediaType2, content.getSource().getUrlOnPlatform() + "?img_index=2");
        media2.setAltText(child2.getAltText());
        media2.setThumbnailSrc(child2.getThumbnailUrl());
        media2.setSrc(child2.getMediaUrl());
        mediaList.add(media2);

        ofc.setMedia(mediaList);

        return ofc;
    }

    @Override
    protected InstagramAuthor getAuthorNormalizerInput() {
        SourceInfo source = getSampleSourceInfo();
        InstagramAuthor author = new InstagramAuthor(source, "testUsername");
        author.setProfilePictureUrl("https://scontent-atl3-2.xx.fbcdn.net/v/t51.2885-15/472386579_1307793557027997_7501062276520195543_n.jpg");
        author.setName("Test Author Name");
        author.setBiography("Test biography");
        author.setWebsite("https://www.jqb.dev");
        author.setFollowersCount(123456);
        author.setMediaCount(123);

        return author;
    }

    @Override
    protected OneFeedAuthor getExpectedAuthorNormalizerOutput() {
        InstagramAuthor author = getAuthorNormalizerInput();
        return new OneFeedAuthor(author.getSource(), author.getHandle(),
            author.getName(), author.getProfilePictureUrl());
    }

    /**
     * Gets sample source info for testing
     * @return a sample {@link SourceInfo} object for testing
     */
    private SourceInfo getSampleSourceInfo() {
        return new SourceInfo("test-instagram-provider-id",
            "test-feed-name", "testID123",
            "https://www.instagram.com/p/abcdefghijk"
        );
    }
}
