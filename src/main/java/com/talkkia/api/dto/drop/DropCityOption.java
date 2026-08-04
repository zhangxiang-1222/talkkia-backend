package com.talkkia.api.dto.drop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropCityOption {
    private Long cityId;
    private String cityCode;
    private String cityName;
}
