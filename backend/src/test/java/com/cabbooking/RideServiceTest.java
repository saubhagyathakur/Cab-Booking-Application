package com.cabbooking;

import com.cabbooking.dto.RideEstimateRequest;
import com.cabbooking.dto.RideEstimateResponse;
import com.cabbooking.dto.RideRequestDto;
import com.cabbooking.dto.RideResponseDto;
import com.cabbooking.entity.*;
import com.cabbooking.exception.BadRequestException;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.RideRepository;
import com.cabbooking.repository.UserRepository;
import com.cabbooking.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RideServiceTest {

    @Autowired
    private RideService rideService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RideRepository rideRepository;

    private User testRider;
    private Driver testDriver;

    @BeforeEach
    void setUp() {
        testRider = userRepository.findByEmail("rider@example.com").orElse(null);
        testDriver = driverRepository.findByStatus(DriverStatus.AVAILABLE).stream().findFirst().orElse(null);
    }

    @Test
    void testRideEstimate() {
        RideEstimateRequest request = new RideEstimateRequest();
        request.setPickupLat(37.7749);
        request.setPickupLng(-122.4194);
        request.setDropLat(37.7833);
        request.setDropLng(-122.4167);
        request.setPromoCode("SAVE20");

        RideEstimateResponse response = rideService.estimateRide(request);

        assertNotNull(response);
        assertTrue(response.getDistanceKm() > 0);
        assertTrue(response.getDurationMinutes() >= 5);
        assertEquals(4, response.getOptions().size());
        assertTrue(response.getOptions().get(0).getDiscount().doubleValue() > 0);
    }

    @Test
    void testRideLifecycle() {
        assertNotNull(testRider, "Test rider should exist from DataInitializer");
        assertNotNull(testDriver, "Test driver should exist from DataInitializer");

        // 1. Request ride
        RideRequestDto requestDto = new RideRequestDto();
        requestDto.setPickupAddress("Powell St, SF");
        requestDto.setPickupLat(37.7844);
        requestDto.setPickupLng(-122.4080);
        requestDto.setDropAddress("Ferry Building, SF");
        requestDto.setDropLat(37.7955);
        requestDto.setDropLng(-122.3937);
        requestDto.setVehicleType(VehicleType.ECONOMY);

        RideResponseDto ride = rideService.requestRide(testRider.getId(), requestDto);
        assertNotNull(ride);
        assertEquals(RideStatus.REQUESTED, ride.getStatus());
        assertNotNull(ride.getOtp());
        String otp = ride.getOtp();

        // 2. Driver accepts ride
        RideResponseDto acceptedRide = rideService.driverAcceptRide(ride.getId(), testDriver.getId());
        assertEquals(RideStatus.ACCEPTED, acceptedRide.getStatus());
        assertEquals(testDriver.getId(), acceptedRide.getDriverId());

        // 3. Driver arrives
        RideResponseDto arrivedRide = rideService.driverArrived(ride.getId(), testDriver.getId());
        assertEquals(RideStatus.ARRIVED, arrivedRide.getStatus());

        // 4. Invalid OTP test
        assertThrows(BadRequestException.class, () -> {
            rideService.startRide(ride.getId(), testDriver.getId(), "99999");
        });

        // 5. Valid OTP start ride
        RideResponseDto startedRide = rideService.startRide(ride.getId(), testDriver.getId(), otp);
        assertEquals(RideStatus.IN_PROGRESS, startedRide.getStatus());

        // 6. Complete ride
        RideResponseDto completedRide = rideService.completeRide(ride.getId(), testDriver.getId());
        assertEquals(RideStatus.COMPLETED, completedRide.getStatus());
        assertNotNull(completedRide.getCompletedAt());
    }
}
