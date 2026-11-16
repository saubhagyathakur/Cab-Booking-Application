package com.cabbooking.dto;

import com.cabbooking.entity.VehicleType;
import java.math.BigDecimal;
import java.util.List;

public class RideEstimateResponse {
    private Double distanceKm;
    private Integer durationMinutes;
    private List<VehicleOption> options;

    public RideEstimateResponse() {
    }

    public RideEstimateResponse(Double distanceKm, Integer durationMinutes, List<VehicleOption> options) {
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.options = options;
    }

    public static class VehicleOption {
        private VehicleType vehicleType;
        private String displayName;
        private BigDecimal fare;
        private BigDecimal discount;
        private BigDecimal finalFare;
        private Integer etaMinutes;
        private Integer availableDriversCount;

        public VehicleOption() {
        }

        public VehicleOption(VehicleType vehicleType, String displayName, BigDecimal fare, BigDecimal discount, BigDecimal finalFare, Integer etaMinutes, Integer availableDriversCount) {
            this.vehicleType = vehicleType;
            this.displayName = displayName;
            this.fare = fare;
            this.discount = discount;
            this.finalFare = finalFare;
            this.etaMinutes = etaMinutes;
            this.availableDriversCount = availableDriversCount;
        }

        public VehicleType getVehicleType() {
            return vehicleType;
        }

        public void setVehicleType(VehicleType vehicleType) {
            this.vehicleType = vehicleType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public BigDecimal getFare() {
            return fare;
        }

        public void setFare(BigDecimal fare) {
            this.fare = fare;
        }

        public BigDecimal getDiscount() {
            return discount;
        }

        public void setDiscount(BigDecimal discount) {
            this.discount = discount;
        }

        public BigDecimal getFinalFare() {
            return finalFare;
        }

        public void setFinalFare(BigDecimal finalFare) {
            this.finalFare = finalFare;
        }

        public Integer getEtaMinutes() {
            return etaMinutes;
        }

        public void setEtaMinutes(Integer etaMinutes) {
            this.etaMinutes = etaMinutes;
        }

        public Integer getAvailableDriversCount() {
            return availableDriversCount;
        }

        public void setAvailableDriversCount(Integer availableDriversCount) {
            this.availableDriversCount = availableDriversCount;
        }
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public List<VehicleOption> getOptions() {
        return options;
    }

    public void setOptions(List<VehicleOption> options) {
        this.options = options;
    }
}
