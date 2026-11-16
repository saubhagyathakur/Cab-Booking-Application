package com.cabbooking.controller;

import com.cabbooking.dto.*;
import com.cabbooking.entity.DriverStatus;
import com.cabbooking.entity.VehicleType;
import com.cabbooking.security.UserPrincipal;
import com.cabbooking.service.DriverService;
import com.cabbooking.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final DriverService driverService;
    private final RideService rideService;

    public DriverController(DriverService driverService, RideService rideService) {
        this.driverService = driverService;
        this.rideService = rideService;
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<DriverResponseDto>>> getNearbyDrivers(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "15.0") double radiusKm,
            @RequestParam(required = false) VehicleType vehicleType) {
        List<DriverResponseDto> drivers = driverService.findNearbyDrivers(lat, lng, radiusKm, vehicleType);
        return ResponseEntity.ok(ApiResponse.ok(drivers));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DriverResponseDto>> getDriverProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DriverResponseDto driver = driverService.getDriverByUserId(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(driver));
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<DriverResponseDto>> updateStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody DriverStatusDto statusDto) {
        DriverResponseDto driver = driverService.getDriverByUserId(userPrincipal.getId());
        DriverResponseDto updated = driverService.updateDriverStatus(driver.getId(), statusDto.getStatus());
        return ResponseEntity.ok(ApiResponse.ok("Status updated successfully", updated));
    }

    @PatchMapping("/location")
    public ResponseEntity<ApiResponse<DriverResponseDto>> updateLocation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody DriverLocationDto locationDto) {
        DriverResponseDto driver = driverService.getDriverByUserId(userPrincipal.getId());
        DriverResponseDto updated = driverService.updateDriverLocation(
                driver.getId(), locationDto.getLatitude(), locationDto.getLongitude()
        );
        return ResponseEntity.ok(ApiResponse.ok("Location updated", updated));
    }

    @GetMapping("/available-requests")
    public ResponseEntity<ApiResponse<List<RideResponseDto>>> getAvailableRideRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DriverResponseDto driver = driverService.getDriverByUserId(userPrincipal.getId());
        List<RideResponseDto> requests = rideService.getPendingRidesForDriver(driver.getId());
        return ResponseEntity.ok(ApiResponse.ok(requests));
    }

    @GetMapping("/active-ride")
    public ResponseEntity<ApiResponse<RideResponseDto>> getActiveRide(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DriverResponseDto driver = driverService.getDriverByUserId(userPrincipal.getId());
        return rideService.getActiveRideForDriver(driver.getId())
                .map(ride -> ResponseEntity.ok(ApiResponse.ok(ride)))
                .orElse(ResponseEntity.ok(ApiResponse.ok("No active ride", null)));
    }

    @GetMapping("/my-rides")
    public ResponseEntity<ApiResponse<List<RideResponseDto>>> getMyRides(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DriverResponseDto driver = driverService.getDriverByUserId(userPrincipal.getId());
        List<RideResponseDto> rides = rideService.getDriverRides(driver.getId());
        return ResponseEntity.ok(ApiResponse.ok(rides));
    }

    @PostMapping("/rides/{rideId}/accept")
    public ResponseEntity<ApiResponse<RideResponseDto>> acceptRide(
            @PathVariable Long rideId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DriverResponseDto driver = driverService.getDriverByUserId(userPrincipal.getId());
        RideResponseDto ride = rideService.driverAcceptRide(rideId, driver.getId());
        return ResponseEntity.ok(ApiResponse.ok("Ride accepted", ride));
    }

    @PostMapping("/rides/{rideId}/arrived")
    public ResponseEntity<ApiResponse<RideResponseDto>> markArrived(
            @PathVariable Long rideId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DriverResponseDto driver = driverService.getDriverByUserId(userPrincipal.getId());
        RideResponseDto ride = rideService.driverArrived(rideId, driver.getId());
        return ResponseEntity.ok(ApiResponse.ok("Driver arrived at pickup", ride));
    }

    @PostMapping("/rides/{rideId}/start")
    public ResponseEntity<ApiResponse<RideResponseDto>> startRide(
            @PathVariable Long rideId,
            @RequestParam String otp,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DriverResponseDto driver = driverService.getDriverByUserId(userPrincipal.getId());
        RideResponseDto ride = rideService.startRide(rideId, driver.getId(), otp);
        return ResponseEntity.ok(ApiResponse.ok("Ride started successfully", ride));
    }

    @PostMapping("/rides/{rideId}/complete")
    public ResponseEntity<ApiResponse<RideResponseDto>> completeRide(
            @PathVariable Long rideId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DriverResponseDto driver = driverService.getDriverByUserId(userPrincipal.getId());
        RideResponseDto ride = rideService.completeRide(rideId, driver.getId());
        return ResponseEntity.ok(ApiResponse.ok("Ride completed successfully", ride));
    }
}
