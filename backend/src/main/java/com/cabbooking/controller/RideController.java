package com.cabbooking.controller;

import com.cabbooking.dto.ApiResponse;
import com.cabbooking.dto.RideEstimateRequest;
import com.cabbooking.dto.RideEstimateResponse;
import com.cabbooking.dto.RideRequestDto;
import com.cabbooking.dto.RideResponseDto;
import com.cabbooking.security.UserPrincipal;
import com.cabbooking.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping("/estimate")
    public ResponseEntity<ApiResponse<RideEstimateResponse>> estimateRide(@Valid @RequestBody RideEstimateRequest request) {
        RideEstimateResponse estimate = rideService.estimateRide(request);
        return ResponseEntity.ok(ApiResponse.ok(estimate));
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<RideResponseDto>> requestRide(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RideRequestDto requestDto) {
        RideResponseDto ride = rideService.requestRide(userPrincipal.getId(), requestDto);
        return ResponseEntity.ok(ApiResponse.ok("Ride requested successfully", ride));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<ApiResponse<RideResponseDto>> getRideById(@PathVariable Long rideId) {
        RideResponseDto ride = rideService.getRideById(rideId);
        return ResponseEntity.ok(ApiResponse.ok(ride));
    }

    @GetMapping("/my-rides")
    public ResponseEntity<ApiResponse<List<RideResponseDto>>> getMyRides(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<RideResponseDto> rides = rideService.getRiderRides(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(rides));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<RideResponseDto>> getActiveRide(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return rideService.getActiveRideForRider(userPrincipal.getId())
                .map(ride -> ResponseEntity.ok(ApiResponse.ok(ride)))
                .orElse(ResponseEntity.ok(ApiResponse.ok("No active ride found", null)));
    }

    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<ApiResponse<RideResponseDto>> cancelRide(
            @PathVariable Long rideId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false, defaultValue = "Cancelled by rider") String reason) {
        RideResponseDto ride = rideService.cancelRide(rideId, userPrincipal.getId(), reason);
        return ResponseEntity.ok(ApiResponse.ok("Ride cancelled", ride));
    }
}
