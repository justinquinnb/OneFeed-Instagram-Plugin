package dev.jqb.onefeed.instagramplugin.apimodel.content;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An Instagram API page result
 */
@Getter
@Setter
@NoArgsConstructor
public class PageResult {

    /**
     * The total pieces of content across all pages (used to know when to stop fetching)
     */
    private int contentCountAcrossPages;

    /**
     * The content contained in this page
     */
    private List<InstagramContent> content;

    /**
     * A cursor to the next page of content
     */
    private String nextPageCursor;

    /**
     * Constructs a new {@code PageResult} with the given {@code content} and cursor to the next
     * page.
     *
     * @param content the content contained in this page
     * @param nextPageCursor a cursor to the next page of content
     */
    public PageResult(List<InstagramContent> content, String nextPageCursor) {
        this.content = content;
        this.nextPageCursor = nextPageCursor;
    }
}
