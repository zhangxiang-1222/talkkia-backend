package com.talkkia.api.dto.reply;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyListItem {
    private Long id;
    private Long userId;
    private String username;
    private String avatarUrl;
    private String audioUrl;
    private Long likeCount;
    private Long dislikeCount;
    private String reaction;
    private Boolean isDropAuthor;
    private Long childReplyCount;
    private Instant createdAt;
}
