package dev.jqb.onefeed.instagramplugin.apimodel.author;

import dev.jqb.onefeed.core.author.AuthorNormalizer;
import dev.jqb.onefeed.core.impl.OneFeedAuthor;

/**
 * A normalizer for {@link InstagramAuthor} --> {@link OneFeedAuthor}
 */
public class InstagramAuthorNormalizer implements AuthorNormalizer<InstagramAuthor, OneFeedAuthor> {

    @Override
    public OneFeedAuthor normalize(InstagramAuthor author) {
        OneFeedAuthor ofa = new OneFeedAuthor();
        ofa.setSource(author.getSource());
        ofa.setName(author.getName());
        ofa.setHandle(author.getHandle());
        ofa.setProfilePicSrc(author.getProfilePictureUrl());

        return ofa;
    }
}
