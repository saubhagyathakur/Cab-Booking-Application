package com.cabbooking.dto;

import jakarta.validation.constraints.NotNull;

public class DriverLocationDto {
    @NotNull(message = "Latitude is required")
    private Double latitude;
    @NotNull(message = "Longitude is required")
    private Double longitude;

    public DriverLocationDto() {
    }

    public DriverLocationDto(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
