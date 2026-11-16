package com.cabbooking.service;

import com.cabbooking.dto.PaymentIntentResponse;
import com.cabbooking.entity.Payment;
import com.cabbooking.entity.PaymentStatus;
import com.cabbooking.entity.Ride;
import com.cabbooking.exception.BadRequestException;
import com.cabbooking.exception.ResourceNotFoundException;
import com.cabbooking.repository.PaymentRepository;
import com.cabbooking.repository.RideRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${app.stripe.secret-key}")
    private String stripeSecretKey;

    private final RideRepository rideRepository;
    private final PaymentRepository paymentRepository;
    private final WebSocketService webSocketService;

    public PaymentService(RideRepository rideRepository,
                          PaymentRepository paymentRepository,
                          WebSocketService webSocketService) {
        this.rideRepository = rideRepository;
        this.paymentRepository = paymentRepository;
        this.webSocketService = webSocketService;
    }

    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.startsWith("sk_test_placeholder")) {
            Stripe.apiKey = stripeSecretKey;
            logger.info("Stripe Java SDK initialized with live/test key.");
        } else {
            logger.warn("Using placeholder/mock Stripe secret key. Payment simulator fallback enabled.");
        }
    }

    @Transactional
    public PaymentIntentResponse createPaymentIntent(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));

        BigDecimal amount = ride.getFinalFare();
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        String clientSecret;
        String intentId;

        // Check if real Stripe key is configured and not placeholder
        if (stripeSecretKey != null && !stripeSecretKey.startsWith("sk_test_placeholder")) {
            try {
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency("usd")
                        .putMetadata("rideId", String.valueOf(ride.getId()))
                        .putMetadata("riderEmail", ride.getRider().getEmail())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();

                PaymentIntent intent = PaymentIntent.create(params);
                clientSecret = intent.getClientSecret();
                intentId = intent.getId();
            } catch (Exception e) {
                logger.error("Failed to create Stripe PaymentIntent: {}. Falling back to mock intent.", e.getMessage());
                intentId = "pi_mock_" + UUID.randomUUID().toString().replace("-", "");
                clientSecret = intentId + "_secret_mock";
            }
        } else {
            // Development fallback mock payment intent
            intentId = "pi_mock_" + UUID.randomUUID().toString().replace("-", "");
            clientSecret = intentId + "_secret_mock";
        }

        Payment payment = paymentRepository.findByRideId(rideId).orElse(new Payment());
        payment.setRide(ride);
        payment.setRider(ride.getRider());
        payment.setAmount(amount);
        payment.setCurrency("USD");
        payment.setStripePaymentIntentId(intentId);
        payment.setStripeClientSecret(clientSecret);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod("CARD");
        paymentRepository.save(payment);

        return new PaymentIntentResponse(clientSecret, intentId, amount, "USD", "requires_payment_method");
    }

    @Transactional
    public Payment confirmPayment(Long rideId, String paymentIntentId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));

        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for ride: " + rideId));

        payment.setStatus(PaymentStatus.PAID);
        payment.setStripePaymentIntentId(paymentIntentId != null ? paymentIntentId : payment.getStripePaymentIntentId());
        payment = paymentRepository.save(payment);

        ride.setPaymentStatus(PaymentStatus.PAID);
        ride = rideRepository.save(ride);

        webSocketService.broadcastRideUpdate(com.cabbooking.dto.RideResponseDto.fromEntity(ride));
        return payment;
    }

    public Payment getPaymentByRideId(Long rideId) {
        return paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for ride: " + rideId));
    }
}
