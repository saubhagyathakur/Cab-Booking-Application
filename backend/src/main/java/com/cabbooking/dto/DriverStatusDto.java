package com.cabbooking.dto;

import com.cabbooking.entity.DriverStatus;
import jakarta.validation.constraints.NotNull;

public class DriverStatusDto {
    @NotNull(message = "Status is required")
    private DriverStatus status;

    public DriverStatusDto() {
    }

    public DriverStatusDto(DriverStatus status) {
        this.status = status;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }
}
