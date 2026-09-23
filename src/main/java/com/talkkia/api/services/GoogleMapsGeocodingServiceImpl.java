package com.talkkia.api.services;

import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.talkkia.api.model.PlaceCoordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class GoogleMapsGeocodingServiceImpl implements GoogleMapsGeocodingService {

    private static final String GEOCODING_BASE_URL = "https://maps.googleapis.com";
    private static final String PLACE_GEOCODING_PATH = "/maps/api/geocode/json";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;
    private final String apiKey;
    private final Duration requestTimeout;

    @Autowired
    public GoogleMapsGeocodingServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${google.maps.server-api-key}") String apiKey
    ) {
        this(webClientBuilder, apiKey, REQUEST_TIMEOUT);
    }

    GoogleMapsGeocodingServiceImpl(
            WebClient.Builder webClientBuilder,
            String apiKey,
            Duration requestTimeout
    ) {
        this.webClient = webClientBuilder.clone()
                .baseUrl(GEOCODING_BASE_URL)
                .build();
        this.apiKey = apiKey;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    @Override
    public PlaceCoordinates findPlaceCoordinates(String placeId) {
        if (!StringUtils.hasText(placeId)) {
            throw new IllegalArgumentException("Google place ID is required");
        }
        if (!isConfigured()) {
            throw new IllegalStateException("Google Maps server API key is not configured");
        }

        JsonNode response;
        try {
            response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(PLACE_GEOCODING_PATH)
                            .queryParam("place_id", placeId.trim())
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(requestTimeout);
        } catch (WebClientRequestException | WebClientResponseException e) {
            throw new IllegalStateException("Google geocoding request failed", e);
        }

        if (response == null) {
            throw new IllegalStateException("Google geocoding returned an empty response");
        }

        String status = response.path("status").asText();
        if (!"OK".equals(status)) {
            throw new IllegalStateException("Google geocoding failed with status: " + status);
        }

        JsonNode location = response.path("results")
                .path(0)
                .path("geometry")
                .path("location");
        if (!location.path("lat").isNumber() || !location.path("lng").isNumber()) {
            throw new IllegalStateException(
                    "Google geocoding response does not contain place coordinates"
            );
        }

        double latitude = location.path("lat").asDouble();
        double longitude = location.path("lng").asDouble();
        validateCoordinates(latitude, longitude);
        return new PlaceCoordinates(latitude, longitude);
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalStateException("Google geocoding returned an invalid latitude");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalStateException("Google geocoding returned an invalid longitude");
        }
    }
}
