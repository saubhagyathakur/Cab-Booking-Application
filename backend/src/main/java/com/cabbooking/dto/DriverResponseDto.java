package com.cabbooking.dto;

import com.cabbooking.entity.Driver;
import com.cabbooking.entity.DriverStatus;
import com.cabbooking.entity.VehicleType;

import java.math.BigDecimal;

public class DriverResponseDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String licenseNumber;
    private String vehicleModel;
    private String vehicleNumber;
    private VehicleType vehicleType;
    private DriverStatus status;
    private Double currentLat;
    private Double currentLng;
    private Double rating;
    private Integer totalRides;
    private BigDecimal totalEarnings;

    public DriverResponseDto() {
    }

    public static DriverResponseDto fromEntity(Driver driver) {
        DriverResponseDto dto = new DriverResponseDto();
        dto.setId(driver.getId());
        if (driver.getUser() != null) {
            dto.setUserId(driver.getUser().getId());
            dto.setName(driver.getUser().getName());
            dto.setEmail(driver.getUser().getEmail());
            dto.setPhone(driver.getUser().getPhone());
        }
        dto.setLicenseNumber(driver.getLicenseNumber());
        dto.setVehicleModel(driver.getVehicleModel());
        dto.setVehicleNumber(driver.getVehicleNumber());
        dto.setVehicleType(driver.getVehicleType());
        dto.setStatus(driver.getStatus());
        dto.setCurrentLat(driver.getCurrentLat());
        dto.setCurrentLng(driver.getCurrentLng());
        dto.setRating(driver.getRating());
        dto.setTotalRides(driver.getTotalRides());
        dto.setTotalEarnings(driver.getTotalEarnings());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
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

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }

    public Double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(Double currentLat) {
        this.currentLat = currentLat;
    }

    public Double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(Double currentLng) {
        this.currentLng = currentLng;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalRides() {
        return totalRides;
    }

    public void setTotalRides(Integer totalRides) {
        this.totalRides = totalRides;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }
}
