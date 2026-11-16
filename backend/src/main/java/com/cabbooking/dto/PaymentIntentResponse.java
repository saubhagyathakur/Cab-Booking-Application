package com.cabbooking.dto;

import java.math.BigDecimal;

public class PaymentIntentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private BigDecimal amount;
    private String currency;
    private String status;

    public PaymentIntentResponse() {
    }

    public PaymentIntentResponse(String clientSecret, String paymentIntentId, BigDecimal amount, String currency, String status) {
        this.clientSecret = clientSecret;
        this.paymentIntentId = paymentIntentId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
