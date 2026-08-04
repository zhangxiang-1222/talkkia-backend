package com.talkkia.api.dto.drop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropListPageRequest {
    private Integer page;
    private Long cityId;
    private String cityCode;
    private Boolean includeCityOptions;
}
