package com.cabbooking.service;

import com.cabbooking.dto.DriverResponseDto;
import com.cabbooking.entity.Driver;
import com.cabbooking.entity.DriverStatus;
import com.cabbooking.entity.VehicleType;
import com.cabbooking.exception.ResourceNotFoundException;
import com.cabbooking.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final WebSocketService webSocketService;

    public DriverService(DriverRepository driverRepository, WebSocketService webSocketService) {
        this.driverRepository = driverRepository;
        this.webSocketService = webSocketService;
    }

    public Driver getDriverEntity(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));
    }

    public DriverResponseDto getDriverById(Long driverId) {
        return DriverResponseDto.fromEntity(getDriverEntity(driverId));
    }

    public DriverResponseDto getDriverByUserId(Long userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user: " + userId));
        return DriverResponseDto.fromEntity(driver);
    }

    @Transactional
    public DriverResponseDto updateDriverLocation(Long driverId, double lat, double lng) {
        Driver driver = getDriverEntity(driverId);
        driver.setCurrentLat(lat);
        driver.setCurrentLng(lng);
        driver = driverRepository.save(driver);

        DriverResponseDto dto = DriverResponseDto.fromEntity(driver);
        webSocketService.broadcastDriverLocation(dto);
        return dto;
    }

    @Transactional
    public DriverResponseDto updateDriverStatus(Long driverId, DriverStatus status) {
        Driver driver = getDriverEntity(driverId);
        driver.setStatus(status);
        driver = driverRepository.save(driver);
        return DriverResponseDto.fromEntity(driver);
    }

    public List<DriverResponseDto> findNearbyDrivers(double lat, double lng, double radiusKm, VehicleType vehicleType) {
        List<Driver> drivers = driverRepository.findAll().stream()
                .filter(d -> d.getStatus() == DriverStatus.AVAILABLE)
                .filter(d -> vehicleType == null || d.getVehicleType() == vehicleType)
                .filter(d -> d.getCurrentLat() != null && d.getCurrentLng() != null)
                .filter(d -> calculateHaversineDistanceKm(lat, lng, d.getCurrentLat(), d.getCurrentLng()) <= radiusKm)
                .collect(Collectors.toList());

        return drivers.stream().map(DriverResponseDto::fromEntity).collect(Collectors.toList());
    }

    public List<DriverResponseDto> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(DriverResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public static double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round((R * c) * 100.0) / 100.0;
    }
}
