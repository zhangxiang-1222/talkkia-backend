package com.talkkia.api.services;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CityCoordinateBackfillRunner implements ApplicationRunner {

    private final CityCoordinateService cityCoordinateService;

    public CityCoordinateBackfillRunner(CityCoordinateService cityCoordinateService) {
        this.cityCoordinateService = cityCoordinateService;
    }

    @Override
    public void run(ApplicationArguments args) {
        cityCoordinateService.backfillMissingCoordinates();
    }
}
