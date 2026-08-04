package com.talkkia.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {
    private Long id;
    private String countryCode;
    private String cityCode;
    private String cityName;
}
