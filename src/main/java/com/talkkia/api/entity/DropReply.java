package com.talkkia.api.entity;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropReply {
    private Long id;
    private Long dropId;
    private Long userId;
    private Long parentReplyId;
    private String audioUrl;
    private String status;
    private Long likeCount;
    private Long dislikeCount;
    private Long childReplyCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}
