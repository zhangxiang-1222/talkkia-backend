package com.talkkia.api.dto.reply;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyReactionResponse {
    private Long replyId;
    private Long likeCount;
    private Long dislikeCount;
    private String reaction;
}
