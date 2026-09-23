package com.talkkia.api.services;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import com.talkkia.api.model.PlaceCoordinates;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoogleMapsGeocodingServiceImplTest {

    @Test
    void shouldReturnCoordinatesFromPlaceGeocodingResponse() {
        AtomicReference<URI> requestUri = new AtomicReference<>();
        GoogleMapsGeocodingServiceImpl service = createService(
                "{\"status\":\"OK\",\"results\":[{\"geometry\":{\"location\":{\"lat\":36.0662299,\"lng\":120.38299}}}]}",
                requestUri
        );

        PlaceCoordinates coordinates = service.findPlaceCoordinates(
                "ChIJa_D4gtUPljUR8_JMYfqCTWE"
        );

        assertEquals(36.0662299, coordinates.getLatitude());
        assertEquals(120.38299, coordinates.getLongitude());
        assertEquals("/maps/api/geocode/json", requestUri.get().getPath());
        assertEquals(
                "place_id=ChIJa_D4gtUPljUR8_JMYfqCTWE&key=server-key",
                requestUri.get().getQuery()
        );
    }

    @Test
    void shouldRejectGoogleErrorStatus() {
        GoogleMapsGeocodingServiceImpl service = createService(
                "{\"status\":\"ZERO_RESULTS\",\"results\":[]}",
                new AtomicReference<>()
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.findPlaceCoordinates("place-id")
        );

        assertEquals("Google geocoding failed with status: ZERO_RESULTS", exception.getMessage());
    }

    @Test
    void shouldRejectMissingPlaceId() {
        GoogleMapsGeocodingServiceImpl service = createService(
                "{\"status\":\"OK\",\"results\":[]}",
                new AtomicReference<>()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.findPlaceCoordinates(" ")
        );
    }

    private GoogleMapsGeocodingServiceImpl createService(
            String responseBody,
            AtomicReference<URI> requestUri
    ) {
        ExchangeFunction exchangeFunction = request -> {
            requestUri.set(request.url());
            return Mono.just(
                    ClientResponse.create(HttpStatus.OK)
                            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .body(responseBody)
                            .build()
            );
        };
        WebClient.Builder webClientBuilder = WebClient.builder()
                .exchangeFunction(exchangeFunction);
        return new GoogleMapsGeocodingServiceImpl(
                webClientBuilder,
                "server-key",
                Duration.ofSeconds(1)
        );
    }
}
