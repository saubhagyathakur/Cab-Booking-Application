package com.cabbooking.controller;

import com.cabbooking.dto.ApiResponse;
import com.cabbooking.security.UserPrincipal;
import com.cabbooking.service.ReceiptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping("/{rideId}/download")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long rideId) {
        byte[] pdfBytes = receiptService.generateInvoicePdf(rideId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-ride-" + rideId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/{rideId}/email")
    public ResponseEntity<ApiResponse<String>> emailInvoice(
            @PathVariable Long rideId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String email) {
        String targetEmail = email != null ? email : userPrincipal.getUsername();
        receiptService.emailReceipt(rideId, targetEmail);
        return ResponseEntity.ok(ApiResponse.ok("Invoice email dispatched to " + targetEmail));
    }
}
