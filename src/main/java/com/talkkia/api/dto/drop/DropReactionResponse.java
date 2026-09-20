package com.talkkia.api.dto.drop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropReactionResponse {
    private Long dropId;
    private Long likeCount;
    private Long funnyCount;
    private Boolean liked;
    private Boolean laughed;
}
