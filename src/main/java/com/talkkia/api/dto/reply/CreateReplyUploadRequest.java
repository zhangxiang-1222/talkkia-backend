package com.talkkia.api.dto.reply;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReplyUploadRequest {
    private Long dropId;
    private Long parentReplyId;
    private String extension;
    private String contentType;
    private Long fileSize;
}
