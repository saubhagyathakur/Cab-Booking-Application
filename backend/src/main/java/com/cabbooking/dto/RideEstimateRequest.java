package com.cabbooking.dto;

import jakarta.validation.constraints.NotNull;

public class RideEstimateRequest {
    @NotNull(message = "Pickup latitude is required")
    private Double pickupLat;
    @NotNull(message = "Pickup longitude is required")
    private Double pickupLng;
    @NotNull(message = "Drop latitude is required")
    private Double dropLat;
    @NotNull(message = "Drop longitude is required")
    private Double dropLng;
    private String promoCode;

    public RideEstimateRequest() {
    }

    public Double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(Double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public Double getPickupLng() {
        return pickupLng;
    }

    public void setPickupLng(Double pickupLng) {
        this.pickupLng = pickupLng;
    }

    public Double getDropLat() {
        return dropLat;
    }

    public void setDropLat(Double dropLat) {
        this.dropLat = dropLat;
    }

    public Double getDropLng() {
        return dropLng;
    }

    public void setDropLng(Double dropLng) {
        this.dropLng = dropLng;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }
}
