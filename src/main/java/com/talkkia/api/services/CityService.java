package com.talkkia.api.services;

import com.talkkia.api.entity.City;

public interface CityService {

    City resolveCity(String countryCode, String cityCode, String cityName);
}
