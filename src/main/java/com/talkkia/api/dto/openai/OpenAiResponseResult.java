package com.talkkia.api.dto.openai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiResponseResult {
    private String text;
    private String responseId;
    private String model;
}
