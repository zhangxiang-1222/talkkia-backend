package com.talkkia.api.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUploadRequest {
    private String extension;
    private String contentType;
    private Long fileSize;
    private String countryCode;
    private String cityName;
    private String cityCode;
}
