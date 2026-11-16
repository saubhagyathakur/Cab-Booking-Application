package com.cabbooking.repository;

import com.cabbooking.entity.Driver;
import com.cabbooking.entity.DriverStatus;
import com.cabbooking.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUserId(Long userId);
    Optional<Driver> findByUserEmail(String email);
    List<Driver> findByStatus(DriverStatus status);
    List<Driver> findByStatusAndVehicleType(DriverStatus status, VehicleType vehicleType);

    // Haversine formula to find available drivers within radius in KM
    @Query(value = "SELECT d.* FROM drivers d " +
            "WHERE d.status = 'AVAILABLE' " +
            "AND (:vehicleType IS NULL OR d.vehicle_type = :vehicleType) " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(d.current_lat)) * " +
            "cos(radians(d.current_lng) - radians(:lng)) + sin(radians(:lat)) * " +
            "sin(radians(d.current_lat)))) <= :radiusKm " +
            "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(d.current_lat)) * " +
            "cos(radians(d.current_lng) - radians(:lng)) + sin(radians(:lat)) * " +
            "sin(radians(d.current_lat)))) ASC", nativeQuery = true)
    List<Driver> findNearbyAvailableDrivers(@Param("lat") double lat,
                                           @Param("lng") double lng,
                                           @Param("radiusKm") double radiusKm,
                                           @Param("vehicleType") String vehicleType);
}
