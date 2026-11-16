package com.cabbooking.controller;

import com.cabbooking.dto.ApiResponse;
import com.cabbooking.dto.AuthRequest;
import com.cabbooking.dto.AuthResponse;
import com.cabbooking.dto.RegisterRequest;
import com.cabbooking.security.UserPrincipal;
import com.cabbooking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserPrincipal>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.ok(userPrincipal));
    }
}
