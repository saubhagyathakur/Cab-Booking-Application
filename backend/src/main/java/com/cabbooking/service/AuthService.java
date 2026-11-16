package com.cabbooking.service;

import com.cabbooking.dto.AuthRequest;
import com.cabbooking.dto.AuthResponse;
import com.cabbooking.dto.RegisterRequest;
import com.cabbooking.entity.Driver;
import com.cabbooking.entity.DriverStatus;
import com.cabbooking.entity.Role;
import com.cabbooking.entity.User;
import com.cabbooking.exception.BadRequestException;
import com.cabbooking.repository.DriverRepository;
import com.cabbooking.repository.UserRepository;
import com.cabbooking.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       DriverRepository driverRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : Role.ROLE_RIDER);
        user = userRepository.save(user);

        Long driverId = null;
        if (user.getRole() == Role.ROLE_DRIVER) {
            Driver driver = new Driver();
            driver.setUser(user);
            driver.setLicenseNumber(request.getLicenseNumber() != null ? request.getLicenseNumber() : "LIC-" + System.currentTimeMillis());
            driver.setVehicleModel(request.getVehicleModel() != null ? request.getVehicleModel() : "Toyota Camry");
            driver.setVehicleNumber(request.getVehicleNumber() != null ? request.getVehicleNumber() : "CAB-" + (System.currentTimeMillis() % 10000));
            driver.setVehicleType(request.getVehicleType() != null ? request.getVehicleType() : com.cabbooking.entity.VehicleType.ECONOMY);
            driver.setStatus(DriverStatus.AVAILABLE);
            // Default initial location near center
            driver.setCurrentLat(37.7749);
            driver.setCurrentLng(-122.4194);
            driver.setRating(5.0);
            driver.setTotalRides(0);
            driver.setTotalEarnings(BigDecimal.ZERO);
            driver = driverRepository.save(driver);
            driverId = driver.getId();
        }

        String token = tokenProvider.generateTokenFromUser(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getName()
        );

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), driverId);
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        Long driverId = null;
        if (user.getRole() == Role.ROLE_DRIVER) {
            driverId = driverRepository.findByUserId(user.getId())
                    .map(Driver::getId)
                    .orElse(null);
        }

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), driverId);
    }
}
