package com.talkkia.api.dto.drop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropReactionState {
    private Boolean liked;
    private Boolean laughed;
}
