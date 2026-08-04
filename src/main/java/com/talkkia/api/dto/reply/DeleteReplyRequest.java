package com.talkkia.api.dto.reply;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteReplyRequest {
    private Long replyId;
}
