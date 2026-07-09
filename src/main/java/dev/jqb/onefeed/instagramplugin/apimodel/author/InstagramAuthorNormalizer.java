package dev.jqb.onefeed.instagramplugin.apimodel.author;

import dev.jqb.onefeed.core.actor.ActorTransformer;
import dev.jqb.onefeed.core.actor.OneFeedActor;

/**
 * A normalizer for {@link InstagramAuthor} --> {@link OneFeedActor}
 */
public class InstagramAuthorNormalizer implements ActorTransformer<InstagramAuthor, OneFeedActor> {

    @Override
    public OneFeedActor transform(InstagramAuthor author) {
        OneFeedActor ofa = new OneFeedActor();
        ofa.setExternalRef(author.getExternalRef());
        ofa.setProviderId(author.getProviderId());
        ofa.setName(author.getName());
        ofa.setHandle(author.getHandle());
        ofa.setProfilePicSrc(author.getProfilePictureUrl());

        return ofa;
    }
}
