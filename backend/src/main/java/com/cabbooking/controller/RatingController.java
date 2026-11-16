package com.cabbooking.controller;

import com.cabbooking.dto.ApiResponse;
import com.cabbooking.dto.RatingRequestDto;
import com.cabbooking.entity.Rating;
import com.cabbooking.security.UserPrincipal;
import com.cabbooking.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Rating>> submitRating(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RatingRequestDto request) {
        Rating rating = ratingService.submitRating(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Rating submitted successfully", rating));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<List<Rating>>> getDriverRatings(@PathVariable Long driverId) {
        List<Rating> ratings = ratingService.getDriverRatings(driverId);
        return ResponseEntity.ok(ApiResponse.ok(ratings));
    }
}
