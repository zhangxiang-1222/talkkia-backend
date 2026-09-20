package com.talkkia.api.dto.drop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropReactionRequest {
    private Long dropId;
    private String reaction;
    private Boolean enabled;
}
