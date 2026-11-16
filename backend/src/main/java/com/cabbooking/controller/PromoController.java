package com.cabbooking.controller;

import com.cabbooking.dto.ApiResponse;
import com.cabbooking.dto.PromoValidationDto;
import com.cabbooking.entity.PromoCode;
import com.cabbooking.repository.PromoCodeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/promos")
public class PromoController {

    private final PromoCodeRepository promoCodeRepository;

    public PromoController(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<PromoValidationDto>> validatePromo(
            @RequestParam String code,
            @RequestParam(required = false, defaultValue = "20.0") BigDecimal fare) {
        Optional<PromoCode> promoOpt = promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim());

        if (promoOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok(
                    new PromoValidationDto(code, false, 0, BigDecimal.ZERO, BigDecimal.ZERO, "Invalid or expired promo code")
            ));
        }

        PromoCode promo = promoOpt.get();
        BigDecimal discount = fare.multiply(BigDecimal.valueOf(promo.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .min(promo.getMaxDiscount());

        return ResponseEntity.ok(ApiResponse.ok(
                new PromoValidationDto(promo.getCode(), true, promo.getDiscountPercent(), promo.getMaxDiscount(), discount, "Promo applied! " + promo.getDiscountPercent() + "% off")
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromoCode>>> getActivePromos() {
        return ResponseEntity.ok(ApiResponse.ok(promoCodeRepository.findAll()));
    }
}
