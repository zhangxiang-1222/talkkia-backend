package com.talkkia.api.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUploadResponse {
    private String objectKey;
    private String uploadUrl;
    private String contentType;
    private Instant expiresAt;
    private Long cityId;
    private Long dropId;
}
