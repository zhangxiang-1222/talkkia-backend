package com.talkkia.api.services;

import java.math.BigDecimal;
import java.util.Arrays;

import com.talkkia.api.entity.City;
import com.talkkia.api.model.PlaceCoordinates;
import com.talkkia.api.repositories.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityCoordinateServiceTest {

    private static final Long CITY_ID = 29L;
    private static final String PLACE_ID = "ChIJa_D4gtUPljUR8_JMYfqCTWE";

    @Mock
    private CityRepository cityRepository;

    @Mock
    private GoogleMapsGeocodingService googleMapsGeocodingService;

    @InjectMocks
    private CityCoordinateService cityCoordinateService;

    @Test
    void shouldPopulateMissingCoordinates() {
        City city = new City(CITY_ID, "CN", PLACE_ID, "Qingdao", null, null);
        when(googleMapsGeocodingService.isConfigured()).thenReturn(true);
        when(googleMapsGeocodingService.findPlaceCoordinates(PLACE_ID))
                .thenReturn(new PlaceCoordinates(36.0662299, 120.38299));
        when(cityRepository.updateCoordinatesIfMissing(
                CITY_ID,
                BigDecimal.valueOf(36.0662299),
                BigDecimal.valueOf(120.38299)
        )).thenReturn(1);

        City result = cityCoordinateService.populateMissingCoordinates(city);

        assertEquals(BigDecimal.valueOf(36.0662299), result.getLatitude());
        assertEquals(BigDecimal.valueOf(120.38299), result.getLongitude());
    }

    @Test
    void shouldNotRequestGoogleForCompleteCoordinates() {
        City city = new City(
                CITY_ID,
                "CN",
                PLACE_ID,
                "Qingdao",
                BigDecimal.valueOf(36.0662299),
                BigDecimal.valueOf(120.38299)
        );

        City result = cityCoordinateService.populateMissingCoordinates(city);

        assertEquals(city, result);
        verifyNoInteractions(googleMapsGeocodingService, cityRepository);
    }

    @Test
    void shouldBackfillAllCitiesWithMissingCoordinates() {
        City firstCity = new City(1L, "CN", "first-place-id", "First", null, null);
        City secondCity = new City(CITY_ID, "CN", PLACE_ID, "Qingdao", null, null);
        when(googleMapsGeocodingService.isConfigured()).thenReturn(true);
        when(cityRepository.findCitiesMissingCoordinates())
                .thenReturn(Arrays.asList(firstCity, secondCity));
        when(googleMapsGeocodingService.findPlaceCoordinates("first-place-id"))
                .thenThrow(new IllegalStateException("ZERO_RESULTS"));
        when(googleMapsGeocodingService.findPlaceCoordinates(PLACE_ID))
                .thenReturn(new PlaceCoordinates(36.0662299, 120.38299));
        when(cityRepository.updateCoordinatesIfMissing(
                CITY_ID,
                BigDecimal.valueOf(36.0662299),
                BigDecimal.valueOf(120.38299)
        )).thenReturn(1);

        cityCoordinateService.backfillMissingCoordinates();

        verify(cityRepository).updateCoordinatesIfMissing(
                CITY_ID,
                BigDecimal.valueOf(36.0662299),
                BigDecimal.valueOf(120.38299)
        );
    }

    @Test
    void shouldSkipStartupBackfillWhenGoogleMapsIsNotConfigured() {
        when(googleMapsGeocodingService.isConfigured()).thenReturn(false);

        cityCoordinateService.backfillMissingCoordinates();

        verifyNoInteractions(cityRepository);
        verify(googleMapsGeocodingService, never()).findPlaceCoordinates(PLACE_ID);
    }
}
