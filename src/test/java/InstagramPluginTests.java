import dev.jqb.onefeed.core.actor.Actor;
import dev.jqb.onefeed.core.actor.OneFeedActor;
import dev.jqb.onefeed.core.content.OneFeedAttachment;
import dev.jqb.onefeed.core.content.OneFeedContent;
import dev.jqb.onefeed.core.content.OneFeedMedia;
import dev.jqb.onefeed.core.feed.FeedId;
import dev.jqb.onefeed.core.platform.ExternalRef;
import dev.jqb.onefeed.core.provider.ProviderConfig;
import dev.jqb.onefeed.instagramplugin.InstagramPlugin;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramAuthor;
import dev.jqb.onefeed.instagramplugin.apimodel.author.InstagramCollaborator;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContent;
import dev.jqb.onefeed.instagramplugin.apimodel.content.InstagramContentChild;
import dev.jqb.onefeed.instagramplugin.apimodel.content.MediaType;
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
        String loginType = dotEnv.get("LOGIN_TYPE").toUpperCase();
        String accessTokenValue = dotEnv.get("ACCESS_TOKEN");
        String isLongLived = dotEnv.get("TOKEN_LONG_LIVED");
        String exchangeForLongLived = dotEnv.get("TOKEN_EXCHANGE_FOR_LONG_LIVED");
        String autoRefresh = dotEnv.get("TOKEN_AUTO_REFRESH");

        String appId = dotEnv.get("APP_ID");
        String appSecret = dotEnv.get("APP_SECRET");
        String useTotalMetricsForNormalization = dotEnv.get("USE_TOTAL_METRICS_FOR_NORMALIZATION");
        boolean useLiteFetchMode = Boolean.parseBoolean(dotEnv.get("USE_LITE_FETCH_MODE"));

        String feedName = dotEnv.get("FEED_NAME");

        HashMap<String, Object> topLevelConfig = new HashMap<>();
        topLevelConfig.put("appId", appId);
        topLevelConfig.put("appSecret", appSecret);
        topLevelConfig.put("useTotalMetricsForNormalization", useTotalMetricsForNormalization);

        HashMap<String, HashMap<String, Object>> feedConfigs = new HashMap<>();
        HashMap<String, Object> testFeedConfig = new HashMap<>();
        testFeedConfig.put("loginType", loginType);
        HashMap<String, String> accessTokenMap = new HashMap<>();
        accessTokenMap.put("value", accessTokenValue);
        accessTokenMap.put("isLongLived", isLongLived);
        accessTokenMap.put("exchangeForLongLived", exchangeForLongLived);
        accessTokenMap.put("autoRefresh", autoRefresh);
        testFeedConfig.put("accessToken", accessTokenMap);
        feedConfigs.put(feedName, testFeedConfig);

        ProviderConfig providerConfig = new ProviderConfig(topLevelConfig, feedConfigs);
        providerConfig.setUsingLiteFetchMode(useLiteFetchMode);

        InstagramPlugin instance = new InstagramPlugin("test", providerConfig);
        instance.start();

        return instance;
    }

    @Override
    protected int getContentPerPageLimit() {
        return 10;
    }

    @Override
    protected InstagramContent getContentNormalizerInput() {
        InstagramContentChild child1 = new InstagramContentChild(
            MediaType.IMAGE,
            "https://scontent-atl3-3.cdninstagram.com/v/t39.30808-6/734179196_122104572974430466_5433913569622665317_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=111&ccb=7-5&_nc_sid=18de74&efg=eyJlZmdfdGFnIjoiQ0FST1VTRUxfSVRFTS5iZXN0X2ltYWdlX3VybGdlbi5DMyJ9&_nc_ohc=LGyZx0DIQ2QQ7kNvwFEyKBv&_nc_oc=AdphKVSpDaNo58itKmgPC3LDKDhRtWUPB9bSCFdb2GNmsNHt4BCPmrrKKOS5ohGLK3Q&_nc_zt=23&_nc_ht=scontent-atl3-3.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=L2VrVTzxG7DelN3y5M7TDQ&oh=00_AQC_SnagLXIs4HuZrmTjblk2XueiclmdEozKGly8rDBzww&oe=6A57457E",
            "1",
            "Child 1 alt text",
            null
        );

        InstagramContentChild child2 = new InstagramContentChild(
            MediaType.IMAGE,
            "https://scontent-atl3-2.cdninstagram.com/v/t39.30808-6/732435320_122104572950430466_4912415370903371051_n.jpg?stp=dst-jpg_e35_tt6&_nc_cat=102&ccb=7-5&_nc_sid=18de74&efg=eyJlZmdfdGFnIjoiQ0FST1VTRUxfSVRFTS5iZXN0X2ltYWdlX3VybGdlbi5DMyJ9&_nc_ohc=6MaI59c_opYQ7kNvwF73aGk&_nc_oc=AdpYhNPuJQu2UN8sGKOI_eTtjwXfy0WAWfbjbhrE03ikzdXUEpNpbT9uuIQzSvkhpsE&_nc_zt=23&_nc_ht=scontent-atl3-2.cdninstagram.com&edm=AM6HXa8EAAAA&_nc_gid=L2VrVTzxG7DelN3y5M7TDQ&oh=00_AQAvmG7LdeqX7rKI3urVidEr21DHcJQFLoFenjuUi59glA&oe=6A5741CD",
            "2",
            "Child 2 alt text",
            null
        );

        // Content itself
        InstagramContent content = new InstagramContent(
            getSampleFeedId(),
            getSampleExternalRef(),
            "testCursor123",
            Instant.ofEpochMilli(1234567890L),
            "testID123",
            List.of(getSampleIgCollaborator())
        );
        content.setMediaType(MediaType.CAROUSEL_ALBUM);
        content.setMediaUrl(child1.getMediaUrl());
        content.setCaption("Test caption");
        content.setAltText(child1.getAltText());
        content.setTotalLikeCount(123);
        content.setChildren(List.of(child1, child2));

        return content;
    }

    @Override
    protected OneFeedContent getExpectedContentNormalizerOutput() {
        InstagramContent content = getContentNormalizerInput();

        // Authors (author + collaborator)
        OneFeedActor author = getExpectedAuthorNormalizerOutput();
        InstagramCollaborator collaborator = getSampleIgCollaborator();
        List<String> authors = new ArrayList<>(2);
        authors.add(author.getExternalRef().id());
        authors.add(collaborator.getExternalRef().id());

        // Media attachments (base media + first child)
        List<InstagramContentChild> children = content.getChildren();
        InstagramContentChild child1 = children.getFirst();
        OneFeedMedia media1 = OneFeedMedia.builder(
            "https://www.instagram.com/p/abcdefghijk?img_index=1", "image/jpeg",
            child1.getMediaUrl(), child1.getAltText()
        ).thumbnailSrc(child1.getThumbnailUrl()).build();
        InstagramContentChild child2 = children.get(1);
        OneFeedMedia media2 = OneFeedMedia.builder(
            "https://www.instagram.com/p/abcdefghijk?img_index=2", "image/jpeg",
            child2.getMediaUrl(), child2.getAltText()
        ).thumbnailSrc(child2.getThumbnailUrl()).build();
        List<OneFeedAttachment> attachments = new ArrayList<>(2);
        attachments.add(media1);
        attachments.add(media2);

        // Content itself
        return OneFeedContent.builder(
            content.getFeedId(), content.getExternalRef(), content.getPublished(), authors
        ).attachments(attachments)
            .nextPageCursor(content.getNextPageCursor().orElse(null))
            .body(content.getCaption())
            .primaryReactionCount(content.getTotalLikeCount())
            .build();
    }

    @Override
    protected InstagramAuthor getAuthorNormalizerInput() {
        InstagramAuthor author = new InstagramAuthor(getSampleProviderId(), getSampleExternalRef(), "testUsername");
        author.setProfilePictureUrl("https://scontent-atl3-3.cdninstagram.com/v/t51.2885-19/472386579_1307793557027997_7501062276520195543_n.jpg?efg=eyJ2ZW5jb2RlX3RhZyI6InByb2ZpbGVfcGljLmRqYW5nby4xMDgwLmMyIn0&_nc_ht=scontent-atl3-3.cdninstagram.com&_nc_cat=109&_nc_oc=Q6cZ2gE0y3Pfd3z2o-df0Nj6HE_tUkZp4sj5eYP43Lo26MjRDrDCy7DrS2iNRyAFCUIcwAI&_nc_ohc=dw412bnW0BgQ7kNvwG1J1HN&_nc_gid=pkfbq7m4mStkxKI0sNzHig&edm=APoiHPcBAAAA&ccb=7-5&oh=00_AQDArKI8MQftYskzC8tN-mlsejEOK4eElGu3j5iaSeinEA&oe=6A5721D2&_nc_sid=22de04");
        author.setName("Test Author Name");
        author.setBiography("Test biography");
        author.setWebsite("https://www.jqb.dev");
        author.setFollowersCount(123456);
        author.setMediaCount(123);

        return author;
    }

    @Override
    protected OneFeedActor getExpectedAuthorNormalizerOutput() {
        InstagramAuthor author = getAuthorNormalizerInput();
        return new OneFeedActor(getSampleProviderId(), getSampleExternalRef(), author.getHandle(),
            author.getName(), author.getProfilePictureUrl());
    }

    /**
     * Gets a sample external reference for testing
     * @return a sample {@link ExternalRef} object for testing
     */
    private static ExternalRef getSampleExternalRef() {
        return new ExternalRef("https://www.instagram.com/p/abcdefghijk", "testID123");
    }

    /**
     * Gets a sample feed ID for testing
     * @return a sample {@link FeedId} object for testing
     */
    private static FeedId getSampleFeedId() {
        return new FeedId(getSampleProviderId(), "test-feed-name");
    }

    /**
     * Gets a sample provider ID for testing
     * @return a sample provider ID for testing
     */
    private static String getSampleProviderId() {
        return "test-instagram-provider-id";
    }

    /**
     * Gets a sample author for testing
     * @return a sample author {@link InstagramAuthor} for testing
     */
    private static InstagramAuthor getSampleIgAuthor() {
        return new InstagramAuthor(getSampleProviderId(), getSampleExternalRef(), "testUsername");
    }

    /**
     * Gets a sample collaborator for testing
     * @return a sample {@link InstagramCollaborator} for testing
     */
    private static InstagramCollaborator getSampleIgCollaborator() {
        return new InstagramCollaborator(
            getSampleProviderId(), getSampleExternalRef(), "testCollaborator1", "Accepted");
    }
}
