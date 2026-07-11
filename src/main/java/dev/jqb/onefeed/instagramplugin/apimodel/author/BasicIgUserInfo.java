package dev.jqb.onefeed.instagramplugin.apimodel.author;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A bundle of a user's basic Instagram account info
 */
@Getter
@AllArgsConstructor
@Setter
@ToString
public class BasicIgUserInfo {
    private String id;
    private String username;
}
