package com.talkkia.api.services;

import java.math.BigDecimal;
import java.util.List;

import com.talkkia.api.entity.City;
import com.talkkia.api.model.PlaceCoordinates;
import com.talkkia.api.repositories.CityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CityCoordinateService {

    private final CityRepository cityRepository;
    private final GoogleMapsGeocodingService googleMapsGeocodingService;

    public CityCoordinateService(
            CityRepository cityRepository,
            GoogleMapsGeocodingService googleMapsGeocodingService
    ) {
        this.cityRepository = cityRepository;
        this.googleMapsGeocodingService = googleMapsGeocodingService;
    }

    public City populateMissingCoordinates(City city) {
        if (city.getLatitude() != null && city.getLongitude() != null) {
            return city;
        }
        if (!googleMapsGeocodingService.isConfigured()) {
            return city;
        }

        try {
            PlaceCoordinates coordinates = googleMapsGeocodingService
                    .findPlaceCoordinates(city.getCityCode());
            int updated = cityRepository.updateCoordinatesIfMissing(
                    city.getId(),
                    BigDecimal.valueOf(coordinates.getLatitude()),
                    BigDecimal.valueOf(coordinates.getLongitude())
            );
            if (updated == 1) {
                city.setLatitude(BigDecimal.valueOf(coordinates.getLatitude()));
                city.setLongitude(BigDecimal.valueOf(coordinates.getLongitude()));
            }
        } catch (Exception e) {
            log.warn(
                    "Failed to populate coordinates for city {}: {}",
                    city.getId(),
                    e.getMessage()
            );
        }
        return city;
    }

    public void backfillMissingCoordinates() {
        if (!googleMapsGeocodingService.isConfigured()) {
            log.warn("City coordinate backfill skipped because Google Maps is not configured");
            return;
        }

        List<City> cities = cityRepository.findCitiesMissingCoordinates();
        for (City city : cities) {
            populateMissingCoordinates(city);
        }
    }
}
