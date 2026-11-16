package com.cabbooking.repository;

import com.cabbooking.entity.Ride;
import com.cabbooking.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByRiderIdOrderByCreatedAtDesc(Long riderId);
    List<Ride> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    List<Ride> findByStatus(RideStatus status);
    
    @Query("SELECT r FROM Ride r WHERE r.rider.id = :riderId AND r.status IN ('REQUESTED', 'ACCEPTED', 'ARRIVED', 'IN_PROGRESS') ORDER BY r.createdAt DESC")
    List<Ride> findActiveRidesForRider(@Param("riderId") Long riderId);

    @Query("SELECT r FROM Ride r WHERE r.driver.id = :driverId AND r.status IN ('ACCEPTED', 'ARRIVED', 'IN_PROGRESS') ORDER BY r.createdAt DESC")
    List<Ride> findActiveRidesForDriver(@Param("driverId") Long driverId);

    long countByStatus(RideStatus status);

    @Query("SELECT COALESCE(SUM(r.finalFare), 0) FROM Ride r WHERE r.status = 'COMPLETED'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(r.finalFare), 0) FROM Ride r WHERE r.driver.id = :driverId AND r.status = 'COMPLETED'")
    BigDecimal calculateDriverTotalEarnings(@Param("driverId") Long driverId);
}
