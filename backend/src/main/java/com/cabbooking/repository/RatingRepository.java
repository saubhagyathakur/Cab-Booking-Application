package com.cabbooking.repository;

import com.cabbooking.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByRideId(Long rideId);
    List<Rating> findByDriverId(Long driverId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.driver.id = :driverId")
    Double getAverageRatingForDriver(@Param("driverId") Long driverId);
}
