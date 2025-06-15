package com.example.demo.controller;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/public/reviews")
public class ReviewController {
    private ReviewService reviewService;
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/total-approved")
    public ApiResponse<Long> totalApprovedReviews() {
        return ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Count of approved reviews")
                .result(reviewService.getTotalApprovedReviews())
                .build();
    }

    @GetMapping("/average-rating")
    public ApiResponse<Double> averageRating() {
        Double averageRating = reviewService.findAverageRatingOfApproved();
        if (averageRating == null) {
            averageRating = 0.0; // Default value if no approved reviews exist
        }
        return ApiResponse.<Double>builder()
                .status(HttpStatus.OK.value())
                .message("Average rating of approved reviews")
                .result(averageRating)
                .build();
    }
}
