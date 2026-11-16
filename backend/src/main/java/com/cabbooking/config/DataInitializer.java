package com.cabbooking.config;

import com.cabbooking.entity.*;
import com.cabbooking.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final RideRepository rideRepository;
    private final PaymentRepository paymentRepository;
    private final RatingRepository ratingRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DriverRepository driverRepository,
                           PromoCodeRepository promoCodeRepository,
                           RideRepository rideRepository,
                           PaymentRepository paymentRepository,
                           RatingRepository ratingRepository,
                           SupportTicketRepository supportTicketRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.promoCodeRepository = promoCodeRepository;
        this.rideRepository = rideRepository;
        this.paymentRepository = paymentRepository;
        this.ratingRepository = ratingRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("Database already seeded. Skipping initial data load.");
            return;
        }

        logger.info("Seeding initial data for Cab Booking Application...");

        // 1. Create Promo Codes
        PromoCode promo1 = new PromoCode("SAVE20", 20, BigDecimal.valueOf(10.00), BigDecimal.valueOf(15.00), LocalDateTime.now().plusMonths(3), true);
        PromoCode promo2 = new PromoCode("WELCOME50", 50, BigDecimal.valueOf(15.00), BigDecimal.ZERO, LocalDateTime.now().plusMonths(6), true);
        PromoCode promo3 = new PromoCode("CAB5", 15, BigDecimal.valueOf(5.00), BigDecimal.ZERO, LocalDateTime.now().plusMonths(1), true);
        promoCodeRepository.save(promo1);
        promoCodeRepository.save(promo2);
        promoCodeRepository.save(promo3);

        // 2. Create Admin User
        User admin = new User();
        admin.setName("Super Admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setPhone("+1-555-0100");
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);

        // 3. Create Demo Rider
        User rider = new User();
        rider.setName("Alex Johnson");
        rider.setEmail("rider@example.com");
        rider.setPassword(passwordEncoder.encode("password123"));
        rider.setPhone("+1-555-0199");
        rider.setRole(Role.ROLE_RIDER);
        rider = userRepository.save(rider);

        // 4. Create Demo Drivers with diverse vehicles and coordinates in San Francisco
        createDriver("John Doe", "driver@example.com", "password123", "+1-555-0201",
                "DL-CA-99481", "Toyota Camry (Silver)", "CAB-1001", VehicleType.ECONOMY,
                37.7749, -122.4194, 4.9, 128, BigDecimal.valueOf(3120.50));

        Driver driver2 = createDriver("Sarah Connor", "driver2@example.com", "password123", "+1-555-0202",
                "DL-CA-88372", "Tesla Model 3 (Midnight Blue)", "CAB-2002", VehicleType.COMFORT,
                37.7833, -122.4167, 4.85, 215, BigDecimal.valueOf(6450.00));

        createDriver("Michael Chen", "driver3@example.com", "password123", "+1-555-0203",
                "DL-CA-77263", "BMW 530i (Black)", "CAB-3003", VehicleType.PREMIER,
                37.7690, -122.4467, 4.95, 310, BigDecimal.valueOf(11240.00));

        createDriver("David Miller", "driver4@example.com", "password123", "+1-555-0204",
                "DL-CA-66154", "Chevrolet Suburban (White)", "CAB-4004", VehicleType.XL,
                37.7899, -122.4010, 4.8, 94, BigDecimal.valueOf(4150.00));

        // 5. Seed a completed historical ride for the rider
        Ride pastRide = new Ride();
        pastRide.setRider(rider);
        pastRide.setDriver(driver2);
        pastRide.setPickupAddress("Market Street & 4th, San Francisco, CA");
        pastRide.setPickupLat(37.7858);
        pastRide.setPickupLng(-122.4065);
        pastRide.setDropAddress("Fisherman's Wharf, San Francisco, CA");
        pastRide.setDropLat(37.8080);
        pastRide.setDropLng(-122.4177);
        pastRide.setVehicleType(VehicleType.COMFORT);
        pastRide.setDistanceKm(3.8);
        pastRide.setDurationMinutes(14);
        pastRide.setFare(BigDecimal.valueOf(18.50));
        pastRide.setDiscount(BigDecimal.valueOf(3.70));
        pastRide.setFinalFare(BigDecimal.valueOf(14.80));
        pastRide.setStatus(RideStatus.COMPLETED);
        pastRide.setOtp("4921");
        pastRide.setPaymentStatus(PaymentStatus.PAID);
        pastRide.setStartedAt(LocalDateTime.now().minusDays(1).minusMinutes(35));
        pastRide.setCompletedAt(LocalDateTime.now().minusDays(1).minusMinutes(21));
        pastRide = rideRepository.save(pastRide);

        Payment payment = new Payment();
        payment.setRide(pastRide);
        payment.setRider(rider);
        payment.setAmount(BigDecimal.valueOf(14.80));
        payment.setCurrency("USD");
        payment.setStripePaymentIntentId("pi_seed_demo_stripe_payment");
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentMethod("CARD");
        paymentRepository.save(payment);

        Rating rating = new Rating();
        rating.setRide(pastRide);
        rating.setRider(rider);
        rating.setDriver(driver2);
        rating.setScore(5);
        rating.setComments("Fantastic ride! Clean car and very smooth driving.");
        ratingRepository.save(rating);

        // 6. Create Support Ticket
        SupportTicket ticket = new SupportTicket();
        ticket.setUser(rider);
        ticket.setRide(pastRide);
        ticket.setSubject("Left jacket in the back seat");
        ticket.setMessage("Hi, I think I accidentally left my blue denim jacket in the back seat during my ride to Fisherman's Wharf.");
        ticket.setStatus("OPEN");
        ticket.setPriority("MEDIUM");
        supportTicketRepository.save(ticket);

        logger.info("Database seeding successfully completed!");
    }

    private Driver createDriver(String name, String email, String password, String phone,
                                String license, String model, String plate, VehicleType vehicleType,
                                double lat, double lng, double rating, int rides, BigDecimal earnings) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole(Role.ROLE_DRIVER);
        user = userRepository.save(user);

        Driver driver = new Driver();
        driver.setUser(user);
        driver.setLicenseNumber(license);
        driver.setVehicleModel(model);
        driver.setVehicleNumber(plate);
        driver.setVehicleType(vehicleType);
        driver.setStatus(DriverStatus.AVAILABLE);
        driver.setCurrentLat(lat);
        driver.setCurrentLng(lng);
        driver.setRating(rating);
        driver.setTotalRides(rides);
        driver.setTotalEarnings(earnings);
        return driverRepository.save(driver);
    }
}
