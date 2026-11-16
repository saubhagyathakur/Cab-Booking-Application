package com.cabbooking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RatingRequestDto {
    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer score;

    private String comments;

    public RatingRequestDto() {
    }

    public RatingRequestDto(Long rideId, Integer score, String comments) {
        this.rideId = rideId;
        this.score = score;
        this.comments = comments;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
