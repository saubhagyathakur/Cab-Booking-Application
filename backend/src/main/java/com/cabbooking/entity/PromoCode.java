package com.cabbooking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;

    @Column(name = "max_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "min_fare", precision = 10, scale = 2)
    private BigDecimal minFare = BigDecimal.ZERO;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PromoCode() {
    }

    public PromoCode(String code, Integer discountPercent, BigDecimal maxDiscount, BigDecimal minFare, LocalDateTime validUntil, Boolean active) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.maxDiscount = maxDiscount;
        this.minFare = minFare;
        this.validUntil = validUntil;
        this.active = active;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
        if (this.minFare == null) this.minFare = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public BigDecimal getMinFare() {
        return minFare;
    }

    public void setMinFare(BigDecimal minFare) {
        this.minFare = minFare;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
