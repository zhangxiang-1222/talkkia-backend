package com.talkkia.api.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteUploadRequest {
    private Long dropId;
}
