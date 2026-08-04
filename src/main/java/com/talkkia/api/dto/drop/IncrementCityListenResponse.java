package com.talkkia.api.dto.drop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncrementCityListenResponse {
    private Long cityId;
    private Long listen;
}
