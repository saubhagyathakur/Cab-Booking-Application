package com.cabbooking.controller;

import com.cabbooking.dto.ApiResponse;
import com.cabbooking.dto.PaymentIntentRequest;
import com.cabbooking.dto.PaymentIntentResponse;
import com.cabbooking.entity.Payment;
import com.cabbooking.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request) {
        PaymentIntentResponse response = paymentService.createPaymentIntent(request.getRideId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Payment>> confirmPayment(
            @RequestParam Long rideId,
            @RequestParam(required = false) String paymentIntentId) {
        Payment payment = paymentService.confirmPayment(rideId, paymentIntentId);
        return ResponseEntity.ok(ApiResponse.ok("Payment confirmed", payment));
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByRideId(@PathVariable Long rideId) {
        Payment payment = paymentService.getPaymentByRideId(rideId);
        return ResponseEntity.ok(ApiResponse.ok(payment));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        // Log webhook receipt and respond with 200 OK
        return ResponseEntity.ok("Webhook received successfully");
    }
}
