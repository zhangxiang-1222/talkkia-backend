package com.talkkia.api.dto.reply;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReplyUploadResponse {
    private Long replyId;
    private String objectKey;
    private String uploadUrl;
    private String contentType;
    private Instant expiresAt;
}
