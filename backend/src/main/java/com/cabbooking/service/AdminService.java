package com.cabbooking.service;

import com.cabbooking.dto.DashboardStatsDto;
import com.cabbooking.dto.DriverResponseDto;
import com.cabbooking.dto.RideResponseDto;
import com.cabbooking.entity.*;
import com.cabbooking.exception.ResourceNotFoundException;
import com.cabbooking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final SupportTicketRepository supportTicketRepository;

    public AdminService(UserRepository userRepository,
                        DriverRepository driverRepository,
                        RideRepository rideRepository,
                        SupportTicketRepository supportTicketRepository) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
        this.supportTicketRepository = supportTicketRepository;
    }

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        List<User> allUsers = userRepository.findAll();
        stats.setTotalUsers(allUsers.size());
        stats.setTotalRiders(allUsers.stream().filter(u -> u.getRole() == Role.ROLE_RIDER).count());
        stats.setTotalDrivers(driverRepository.count());
        stats.setActiveDrivers(driverRepository.findByStatus(DriverStatus.AVAILABLE).size());

        List<Ride> allRides = rideRepository.findAll();
        stats.setTotalRides(allRides.size());
        stats.setActiveRides(allRides.stream()
                .filter(r -> r.getStatus() == RideStatus.REQUESTED ||
                        r.getStatus() == RideStatus.ACCEPTED ||
                        r.getStatus() == RideStatus.ARRIVED ||
                        r.getStatus() == RideStatus.IN_PROGRESS)
                .count());
        stats.setCompletedRides(rideRepository.countByStatus(RideStatus.COMPLETED));
        stats.setCancelledRides(rideRepository.countByStatus(RideStatus.CANCELLED));

        BigDecimal revenue = rideRepository.calculateTotalRevenue();
        stats.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        Map<String, Long> byVehicle = new HashMap<>();
        for (VehicleType vt : VehicleType.values()) {
            long count = allRides.stream().filter(r -> r.getVehicleType() == vt).count();
            byVehicle.put(vt.name(), count);
        }
        stats.setRidesByVehicleType(byVehicle);

        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<DriverResponseDto> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(DriverResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RideResponseDto> getAllRides() {
        return rideRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(RideResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SupportTicket> getAllSupportTickets() {
        return supportTicketRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public SupportTicket createSupportTicket(Long userId, SupportTicket ticket) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        ticket.setUser(user);
        return supportTicketRepository.save(ticket);
    }

    @Transactional
    public SupportTicket resolveSupportTicket(Long ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        ticket.setStatus("RESOLVED");
        ticket.setResolvedAt(LocalDateTime.now());
        return supportTicketRepository.save(ticket);
    }
}
