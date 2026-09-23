package com.talkkia.api.services;

import com.talkkia.api.entity.City;
import com.talkkia.api.repositories.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final CityCoordinateService cityCoordinateService;

    public CityServiceImpl(
            CityRepository cityRepository,
            CityCoordinateService cityCoordinateService
    ) {
        this.cityRepository = cityRepository;
        this.cityCoordinateService = cityCoordinateService;
    }

    @Override
    public City resolveCity(String countryCode, String cityCode, String cityName) {
        if (!StringUtils.hasText(countryCode)
                || !StringUtils.hasText(cityCode)
                || !StringUtils.hasText(cityName)) {
            throw new IllegalArgumentException("Country code, city code, and city name are required");
        }

        City city = cityRepository.resolveCity(countryCode, cityCode, cityName);
        return cityCoordinateService.populateMissingCoordinates(city);
    }
}
