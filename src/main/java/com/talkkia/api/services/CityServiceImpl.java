package com.talkkia.api.services;

import com.talkkia.api.entity.City;
import com.talkkia.api.repositories.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public City resolveCity(String countryCode, String cityCode, String cityName) {
        if (!StringUtils.hasText(countryCode)
                || !StringUtils.hasText(cityCode)
                || !StringUtils.hasText(cityName)) {
            throw new IllegalArgumentException("Country code, city code, and city name are required");
        }

        return cityRepository.resolveCity(countryCode, cityCode, cityName);
    }
}
