package com.cabbooking.dto;

import java.math.BigDecimal;

public class PromoValidationDto {
    private String code;
    private boolean valid;
    private Integer discountPercent;
    private BigDecimal maxDiscount;
    private BigDecimal discountApplied;
    private String message;

    public PromoValidationDto() {
    }

    public PromoValidationDto(String code, boolean valid, Integer discountPercent, BigDecimal maxDiscount, BigDecimal discountApplied, String message) {
        this.code = code;
        this.valid = valid;
        this.discountPercent = discountPercent;
        this.maxDiscount = maxDiscount;
        this.discountApplied = discountApplied;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
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

    public BigDecimal getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(BigDecimal discountApplied) {
        this.discountApplied = discountApplied;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
