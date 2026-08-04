package com.talkkia.api.services;

import java.util.NoSuchElementException;

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

    @Override
    public Long incrementListen(Long cityId) {
        if (cityId == null || cityId <= 0) {
            throw new IllegalArgumentException("City ID is required");
        }

        Long listen = cityRepository.incrementListen(cityId);
        if (listen == null) {
            throw new NoSuchElementException("City not found");
        }
        return listen;
    }
}
