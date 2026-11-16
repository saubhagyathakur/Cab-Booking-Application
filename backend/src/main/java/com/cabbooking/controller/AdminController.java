package com.cabbooking.controller;

import com.cabbooking.dto.ApiResponse;
import com.cabbooking.dto.DashboardStatsDto;
import com.cabbooking.dto.DriverResponseDto;
import com.cabbooking.dto.RideResponseDto;
import com.cabbooking.entity.SupportTicket;
import com.cabbooking.entity.User;
import com.cabbooking.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()));
    }

    @GetMapping("/drivers")
    public ResponseEntity<ApiResponse<List<DriverResponseDto>>> getAllDrivers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllDrivers()));
    }

    @GetMapping("/rides")
    public ResponseEntity<ApiResponse<List<RideResponseDto>>> getAllRides() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllRides()));
    }

    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<List<SupportTicket>>> getTickets() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllSupportTickets()));
    }

    @PostMapping("/tickets/{ticketId}/resolve")
    public ResponseEntity<ApiResponse<SupportTicket>> resolveTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.ok("Ticket resolved", adminService.resolveSupportTicket(ticketId)));
    }
}
