package com.talkkia.api.model;

public class PlaceCoordinates {

    private final double latitude;
    private final double longitude;

    public PlaceCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
