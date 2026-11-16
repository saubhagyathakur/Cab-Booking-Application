package com.cabbooking.dto;

import com.cabbooking.entity.PaymentStatus;
import com.cabbooking.entity.Ride;
import com.cabbooking.entity.RideStatus;
import com.cabbooking.entity.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RideResponseDto {
    private Long id;
    private Long riderId;
    private String riderName;
    private String riderPhone;
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String vehicleModel;
    private String vehicleNumber;
    private Double driverRating;
    private Double driverLat;
    private Double driverLng;

    private String pickupAddress;
    private Double pickupLat;
    private Double pickupLng;
    private String dropAddress;
    private Double dropLat;
    private Double dropLng;

    private VehicleType vehicleType;
    private Double distanceKm;
    private Integer durationMinutes;
    private BigDecimal fare;
    private BigDecimal discount;
    private BigDecimal finalFare;

    private RideStatus status;
    private String otp;
    private PaymentStatus paymentStatus;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public RideResponseDto() {
    }

    public static RideResponseDto fromEntity(Ride ride) {
        RideResponseDto dto = new RideResponseDto();
        dto.setId(ride.getId());
        if (ride.getRider() != null) {
            dto.setRiderId(ride.getRider().getId());
            dto.setRiderName(ride.getRider().getName());
            dto.setRiderPhone(ride.getRider().getPhone());
        }
        if (ride.getDriver() != null) {
            dto.setDriverId(ride.getDriver().getId());
            if (ride.getDriver().getUser() != null) {
                dto.setDriverName(ride.getDriver().getUser().getName());
                dto.setDriverPhone(ride.getDriver().getUser().getPhone());
            }
            dto.setVehicleModel(ride.getDriver().getVehicleModel());
            dto.setVehicleNumber(ride.getDriver().getVehicleNumber());
            dto.setDriverRating(ride.getDriver().getRating());
            dto.setDriverLat(ride.getDriver().getCurrentLat());
            dto.setDriverLng(ride.getDriver().getCurrentLng());
        }
        dto.setPickupAddress(ride.getPickupAddress());
        dto.setPickupLat(ride.getPickupLat());
        dto.setPickupLng(ride.getPickupLng());
        dto.setDropAddress(ride.getDropAddress());
        dto.setDropLat(ride.getDropLat());
        dto.setDropLng(ride.getDropLng());
        dto.setVehicleType(ride.getVehicleType());
        dto.setDistanceKm(ride.getDistanceKm());
        dto.setDurationMinutes(ride.getDurationMinutes());
        dto.setFare(ride.getFare());
        dto.setDiscount(ride.getDiscount());
        dto.setFinalFare(ride.getFinalFare());
        dto.setStatus(ride.getStatus());
        dto.setOtp(ride.getOtp());
        dto.setPaymentStatus(ride.getPaymentStatus());
        dto.setCancellationReason(ride.getCancellationReason());
        dto.setCreatedAt(ride.getCreatedAt());
        dto.setStartedAt(ride.getStartedAt());
        dto.setCompletedAt(ride.getCompletedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRiderId() {
        return riderId;
    }

    public void setRiderId(Long riderId) {
        this.riderId = riderId;
    }

    public String getRiderName() {
        return riderName;
    }

    public void setRiderName(String riderName) {
        this.riderName = riderName;
    }

    public String getRiderPhone() {
        return riderPhone;
    }

    public void setRiderPhone(String riderPhone) {
        this.riderPhone = riderPhone;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Double getDriverRating() {
        return driverRating;
    }

    public void setDriverRating(Double driverRating) {
        this.driverRating = driverRating;
    }

    public Double getDriverLat() {
        return driverLat;
    }

    public void setDriverLat(Double driverLat) {
        this.driverLat = driverLat;
    }

    public Double getDriverLng() {
        return driverLng;
    }

    public void setDriverLng(Double driverLng) {
        this.driverLng = driverLng;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
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

    public String getDropAddress() {
        return dropAddress;
    }

    public void setDropAddress(String dropAddress) {
        this.dropAddress = dropAddress;
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

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
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

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
