package dev.jqb.onefeed.instagramplugin.apimodel;

import java.util.List;

/**
 * An Instagram API page result
 */
public class PageResult {
    private int contentCountAcrossPages;

    private List<InstagramContent> content;

    private String nextPageCursor;
}
