package com.talkkia.api.services;

import com.talkkia.api.model.PlaceCoordinates;

public interface GoogleMapsGeocodingService {

    boolean isConfigured();

    PlaceCoordinates findPlaceCoordinates(String placeId);
}
