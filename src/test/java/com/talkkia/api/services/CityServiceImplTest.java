package com.talkkia.api.services;

import com.talkkia.api.entity.City;
import com.talkkia.api.repositories.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityServiceImplTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CityCoordinateService cityCoordinateService;

    @InjectMocks
    private CityServiceImpl cityService;

    @Test
    void shouldPopulateCoordinatesAfterResolvingCity() {
        City resolvedCity = new City(1L, "CN", "place-id", "Qingdao", null, null);
        City populatedCity = new City(1L, "CN", "place-id", "Qingdao", null, null);
        when(cityRepository.resolveCity("CN", "place-id", "Qingdao"))
                .thenReturn(resolvedCity);
        when(cityCoordinateService.populateMissingCoordinates(resolvedCity))
                .thenReturn(populatedCity);

        City result = cityService.resolveCity("CN", "place-id", "Qingdao");

        assertSame(populatedCity, result);
        verify(cityCoordinateService).populateMissingCoordinates(resolvedCity);
    }
}
