package com.cabbooking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(name = "vehicle_model", nullable = false, length = 100)
    private String vehicleModel;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 50)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 30)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriverStatus status = DriverStatus.OFFLINE;

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;

    @Column(nullable = false)
    private Double rating = 5.0;

    @Column(name = "total_rides", nullable = false)
    private Integer totalRides = 0;

    @Column(name = "total_earnings", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Driver() {
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.rating == null) this.rating = 5.0;
        if (this.totalRides == null) this.totalRides = 0;
        if (this.totalEarnings == null) this.totalEarnings = BigDecimal.ZERO;
        if (this.status == null) this.status = DriverStatus.OFFLINE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
