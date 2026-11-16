package com.cabbooking.service;

import com.cabbooking.dto.RideEstimateRequest;
import com.cabbooking.dto.RideEstimateResponse;
import com.cabbooking.dto.RideRequestDto;
import com.cabbooking.dto.RideResponseDto;
import com.cabbooking.entity.*;
import com.cabbooking.exception.BadRequestException;
import com.cabbooking.exception.ResourceNotFoundException;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.PromoCodeRepository;
import com.cabbooking.repository.RideRepository;
import com.cabbooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RideService {

    private static final Logger logger = LoggerFactory.getLogger(RideService.class);
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final WebSocketService webSocketService;
    private final SecureRandom secureRandom = new SecureRandom();

    public RideService(RideRepository rideRepository,
                       UserRepository userRepository,
                       DriverRepository driverRepository,
                       PromoCodeRepository promoCodeRepository,
                       WebSocketService webSocketService) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.promoCodeRepository = promoCodeRepository;
        this.webSocketService = webSocketService;
    }

    public RideEstimateResponse estimateRide(RideEstimateRequest request) {
        double distanceKm = DriverService.calculateHaversineDistanceKm(
                request.getPickupLat(), request.getPickupLng(),
                request.getDropLat(), request.getDropLng()
        );
        // Minimum distance fallback if coords are identical
        if (distanceKm < 0.5) distanceKm = 0.5;

        // Approx 30 km/h in city traffic -> ~2 mins per km, min 5 mins
        int durationMinutes = Math.max(5, (int) Math.round(distanceKm * 2.0));

        // Check promo
        BigDecimal discountPercent = BigDecimal.ZERO;
        BigDecimal maxDiscount = BigDecimal.ZERO;
        if (request.getPromoCode() != null && !request.getPromoCode().trim().isEmpty()) {
            Optional<PromoCode> promoOpt = promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue(request.getPromoCode().trim());
            if (promoOpt.isPresent()) {
                discountPercent = BigDecimal.valueOf(promoOpt.get().getDiscountPercent());
                maxDiscount = promoOpt.get().getMaxDiscount();
            }
        }

        List<Driver> allAvailableDrivers = driverRepository.findByStatus(DriverStatus.AVAILABLE);

        List<RideEstimateResponse.VehicleOption> options = new ArrayList<>();
        for (VehicleType vt : VehicleType.values()) {
            double rawFare = vt.getBaseFare() + (distanceKm * vt.getPerKmRate());
            BigDecimal fare = BigDecimal.valueOf(rawFare).setScale(2, RoundingMode.HALF_UP);

            BigDecimal discount = BigDecimal.ZERO;
            if (discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal calculatedDiscount = fare.multiply(discountPercent)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                discount = calculatedDiscount.min(maxDiscount);
            }
            BigDecimal finalFare = fare.subtract(discount).max(BigDecimal.valueOf(2.00));

            // Available drivers of this vehicle type
            List<Driver> matchingDrivers = allAvailableDrivers.stream()
                    .filter(d -> d.getVehicleType() == vt)
                    .collect(Collectors.toList());

            // Estimated arrival time of nearest driver
            int etaMinutes = 4; // default
            if (!matchingDrivers.isEmpty()) {
                double minDriverDist = matchingDrivers.stream()
                        .filter(d -> d.getCurrentLat() != null && d.getCurrentLng() != null)
                        .mapToDouble(d -> DriverService.calculateHaversineDistanceKm(
                                request.getPickupLat(), request.getPickupLng(),
                                d.getCurrentLat(), d.getCurrentLng()
                        ))
                        .min()
                        .orElse(2.0);
                etaMinutes = Math.max(2, (int) Math.round(minDriverDist * 2.5));
            }

            options.add(new RideEstimateResponse.VehicleOption(
                    vt,
                    vt.name().substring(0, 1) + vt.name().substring(1).toLowerCase(),
                    fare,
                    discount,
                    finalFare,
                    etaMinutes,
                    matchingDrivers.size()
            ));
        }

        return new RideEstimateResponse(distanceKm, durationMinutes, options);
    }

    @Transactional
    public RideResponseDto requestRide(Long riderId, RideRequestDto requestDto) {
        User rider = userRepository.findById(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found: " + riderId));

        // Check if rider already has an active ride
        List<Ride> activeRides = rideRepository.findActiveRidesForRider(riderId);
        if (!activeRides.isEmpty()) {
            throw new BadRequestException("You already have an active ride request in progress (Ride #" + activeRides.get(0).getId() + ")");
        }

        double distanceKm = DriverService.calculateHaversineDistanceKm(
                requestDto.getPickupLat(), requestDto.getPickupLng(),
                requestDto.getDropLat(), requestDto.getDropLng()
        );
        if (distanceKm < 0.5) distanceKm = 0.5;

        int durationMinutes = Math.max(5, (int) Math.round(distanceKm * 2.0));

        VehicleType vt = requestDto.getVehicleType();
        double rawFare = vt.getBaseFare() + (distanceKm * vt.getPerKmRate());
        BigDecimal fare = BigDecimal.valueOf(rawFare).setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = BigDecimal.ZERO;
        if (requestDto.getPromoCode() != null && !requestDto.getPromoCode().trim().isEmpty()) {
            Optional<PromoCode> promoOpt = promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue(requestDto.getPromoCode().trim());
            if (promoOpt.isPresent()) {
                BigDecimal pct = BigDecimal.valueOf(promoOpt.get().getDiscountPercent());
                BigDecimal calculatedDiscount = fare.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                discount = calculatedDiscount.min(promoOpt.get().getMaxDiscount());
            }
        }
        BigDecimal finalFare = fare.subtract(discount).max(BigDecimal.valueOf(2.00));

        // Generate 4-digit numeric OTP for security verification
        String otp = String.format("%04d", secureRandom.nextInt(10000));

        Ride ride = new Ride();
        ride.setRider(rider);
        ride.setPickupAddress(requestDto.getPickupAddress());
        ride.setPickupLat(requestDto.getPickupLat());
        ride.setPickupLng(requestDto.getPickupLng());
        ride.setDropAddress(requestDto.getDropAddress());
        ride.setDropLat(requestDto.getDropLat());
        ride.setDropLng(requestDto.getDropLng());
        ride.setVehicleType(vt);
        ride.setDistanceKm(distanceKm);
        ride.setDurationMinutes(durationMinutes);
        ride.setFare(fare);
        ride.setDiscount(discount);
        ride.setFinalFare(finalFare);
        ride.setStatus(RideStatus.REQUESTED);
        ride.setOtp(otp);
        ride.setPaymentStatus(PaymentStatus.UNPAID);

        ride = rideRepository.save(ride);
        RideResponseDto responseDto = RideResponseDto.fromEntity(ride);

        // Notify nearby available drivers
        List<Driver> nearbyDrivers = driverRepository.findByStatus(DriverStatus.AVAILABLE).stream()
                .filter(d -> d.getVehicleType() == vt || d.getVehicleType() == VehicleType.ECONOMY)
                .collect(Collectors.toList());

        for (Driver driver : nearbyDrivers) {
            webSocketService.notifyDriverNewRide(driver.getId(), responseDto);
        }

        webSocketService.broadcastRideUpdate(responseDto);
        return responseDto;
    }

    @Transactional
    public RideResponseDto driverAcceptRide(Long rideId, Long driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new BadRequestException("Ride is no longer available. Status: " + ride.getStatus());
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));

        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);
        driver.setStatus(DriverStatus.BUSY);
        driverRepository.save(driver);

        ride = rideRepository.save(ride);
        RideResponseDto dto = RideResponseDto.fromEntity(ride);
        webSocketService.broadcastRideUpdate(dto);
        return dto;
    }

    @Transactional
    public RideResponseDto driverArrived(Long rideId, Long driverId) {
        Ride ride = getRideEntity(rideId);
        validateRideDriver(ride, driverId);

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new BadRequestException("Ride cannot transition to ARRIVED from status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.ARRIVED);
        ride = rideRepository.save(ride);

        RideResponseDto dto = RideResponseDto.fromEntity(ride);
        webSocketService.broadcastRideUpdate(dto);
        return dto;
    }

    @Transactional
    public RideResponseDto startRide(Long rideId, Long driverId, String otp) {
        Ride ride = getRideEntity(rideId);
        validateRideDriver(ride, driverId);

        if (ride.getStatus() != RideStatus.ARRIVED && ride.getStatus() != RideStatus.ACCEPTED) {
            throw new BadRequestException("Ride cannot be started from status: " + ride.getStatus());
        }

        if (ride.getOtp() != null && !ride.getOtp().equals(otp.trim())) {
            throw new BadRequestException("Invalid ride verification OTP. Please ask rider for correct PIN.");
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());
        ride = rideRepository.save(ride);

        RideResponseDto dto = RideResponseDto.fromEntity(ride);
        webSocketService.broadcastRideUpdate(dto);
        return dto;
    }

    @Transactional
    public RideResponseDto completeRide(Long rideId, Long driverId) {
        Ride ride = getRideEntity(rideId);
        validateRideDriver(ride, driverId);

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new BadRequestException("Cannot complete ride that is not IN_PROGRESS");
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride = rideRepository.save(ride);

        // Update driver stats
        Driver driver = ride.getDriver();
        if (driver != null) {
            driver.setStatus(DriverStatus.AVAILABLE);
            driver.setTotalRides(driver.getTotalRides() + 1);
            driver.setTotalEarnings(driver.getTotalEarnings().add(ride.getFinalFare()));
            driverRepository.save(driver);
        }

        RideResponseDto dto = RideResponseDto.fromEntity(ride);
        webSocketService.broadcastRideUpdate(dto);
        return dto;
    }

    @Transactional
    public RideResponseDto cancelRide(Long rideId, Long userId, String reason) {
        Ride ride = getRideEntity(rideId);

        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new BadRequestException("Ride is already " + ride.getStatus());
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancellationReason(reason != null ? reason : "Cancelled by user");

        if (ride.getDriver() != null) {
            ride.getDriver().setStatus(DriverStatus.AVAILABLE);
            driverRepository.save(ride.getDriver());
        }

        ride = rideRepository.save(ride);
        RideResponseDto dto = RideResponseDto.fromEntity(ride);
        webSocketService.broadcastRideUpdate(dto);
        return dto;
    }

    public Ride getRideEntity(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));
    }

    public RideResponseDto getRideById(Long rideId) {
        return RideResponseDto.fromEntity(getRideEntity(rideId));
    }

    public List<RideResponseDto> getRiderRides(Long riderId) {
        return rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId).stream()
                .map(RideResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RideResponseDto> getDriverRides(Long driverId) {
        return rideRepository.findByDriverIdOrderByCreatedAtDesc(driverId).stream()
                .map(RideResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<RideResponseDto> getActiveRideForRider(Long riderId) {
        List<Ride> rides = rideRepository.findActiveRidesForRider(riderId);
        return rides.isEmpty() ? Optional.empty() : Optional.of(RideResponseDto.fromEntity(rides.get(0)));
    }

    public Optional<RideResponseDto> getActiveRideForDriver(Long driverId) {
        List<Ride> rides = rideRepository.findActiveRidesForDriver(driverId);
        return rides.isEmpty() ? Optional.empty() : Optional.of(RideResponseDto.fromEntity(rides.get(0)));
    }

    public List<RideResponseDto> getPendingRidesForDriver(Long driverId) {
        return rideRepository.findByStatus(RideStatus.REQUESTED).stream()
                .map(RideResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    private void validateRideDriver(Ride ride, Long driverId) {
        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driverId)) {
            throw new BadRequestException("You are not the assigned driver for this ride");
        }
    }
}
