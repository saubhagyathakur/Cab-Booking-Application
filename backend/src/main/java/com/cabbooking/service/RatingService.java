package com.cabbooking.service;

import com.cabbooking.dto.RatingRequestDto;
import com.cabbooking.entity.Driver;
import com.cabbooking.entity.Rating;
import com.cabbooking.entity.Ride;
import com.cabbooking.entity.RideStatus;
import com.cabbooking.exception.BadRequestException;
import com.cabbooking.exception.ResourceNotFoundException;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.RatingRepository;
import com.cabbooking.repository.RideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;

    public RatingService(RatingRepository ratingRepository,
                         RideRepository rideRepository,
                         DriverRepository driverRepository) {
        this.ratingRepository = ratingRepository;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public Rating submitRating(Long riderId, RatingRequestDto request) {
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + request.getRideId()));

        if (ride.getStatus() != RideStatus.COMPLETED) {
            throw new BadRequestException("You can only rate a ride that is COMPLETED");
        }

        if (ride.getDriver() == null) {
            throw new BadRequestException("No driver was assigned to this ride");
        }

        if (ratingRepository.findByRideId(ride.getId()).isPresent()) {
            throw new BadRequestException("Ride has already been rated");
        }

        Rating rating = new Rating();
        rating.setRide(ride);
        rating.setRider(ride.getRider());
        rating.setDriver(ride.getDriver());
        rating.setScore(request.getScore());
        rating.setComments(request.getComments());
        rating = ratingRepository.save(rating);

        // Recalculate driver average rating
        Driver driver = ride.getDriver();
        Double avgRating = ratingRepository.getAverageRatingForDriver(driver.getId());
        if (avgRating != null) {
            double roundedRating = BigDecimal.valueOf(avgRating)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
            driver.setRating(roundedRating);
            driverRepository.save(driver);
        }

        return rating;
    }

    public List<Rating> getDriverRatings(Long driverId) {
        return ratingRepository.findByDriverId(driverId);
    }
}
