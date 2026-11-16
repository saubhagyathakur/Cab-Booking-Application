package com.cabbooking.dto;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardStatsDto {
    private long totalUsers;
    private long totalRiders;
    private long totalDrivers;
    private long activeDrivers;
    private long totalRides;
    private long activeRides;
    private long completedRides;
    private long cancelledRides;
    private BigDecimal totalRevenue;
    private Map<String, Long> ridesByVehicleType;

    public DashboardStatsDto() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalRiders() {
        return totalRiders;
    }

    public void setTotalRiders(long totalRiders) {
        this.totalRiders = totalRiders;
    }

    public long getTotalDrivers() {
        return totalDrivers;
    }

    public void setTotalDrivers(long totalDrivers) {
        this.totalDrivers = totalDrivers;
    }

    public long getActiveDrivers() {
        return activeDrivers;
    }

    public void setActiveDrivers(long activeDrivers) {
        this.activeDrivers = activeDrivers;
    }

    public long getTotalRides() {
        return totalRides;
    }

    public void setTotalRides(long totalRides) {
        this.totalRides = totalRides;
    }

    public long getActiveRides() {
        return activeRides;
    }

    public void setActiveRides(long activeRides) {
        this.activeRides = activeRides;
    }

    public long getCompletedRides() {
        return completedRides;
    }

    public void setCompletedRides(long completedRides) {
        this.completedRides = completedRides;
    }

    public long getCancelledRides() {
        return cancelledRides;
    }

    public void setCancelledRides(long cancelledRides) {
        this.cancelledRides = cancelledRides;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Map<String, Long> getRidesByVehicleType() {
        return ridesByVehicleType;
    }

    public void setRidesByVehicleType(Map<String, Long> ridesByVehicleType) {
        this.ridesByVehicleType = ridesByVehicleType;
    }
}
