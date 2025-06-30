package com.example.demo.controller;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.model.Review;
import com.example.demo.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/public/reviews")
public class ReviewController {
    private final ReviewService reviewService;

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

    // Thêm mới: Lấy tất cả đánh giá của một phim
    @GetMapping("/movie/{movieId}")
    public ApiResponse<List<Review>> getReviewsByMovieId(@PathVariable Long movieId) {
        return ApiResponse.<List<Review>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách đánh giá của phim ID " + movieId)
                .result(reviewService.getReviewsByMovieId(movieId))
                .build();
    }

    // Thêm mới: Đếm số đánh giá được duyệt của một phim
    @GetMapping("/movie/total-approved/{movieId}")
    public ApiResponse<Long> totalApprovedReviewsByMovieId(@PathVariable Long movieId) {
        return ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Số lượng đánh giá được duyệt của phim ID " + movieId)
                .result(reviewService.getTotalApprovedReviewsByMovieId(movieId))
                .build();
    }

    // Thêm mới: Tính trung bình sao của một phim
    @GetMapping("/movie/average-rating/{movieId}")
    public ApiResponse<Double> averageRatingByMovieId(@PathVariable Long movieId) {
        return ApiResponse.<Double>builder()
                .status(HttpStatus.OK.value())
                .message("Trung bình điểm đánh giá được duyệt của phim ID " + movieId)
                .result(reviewService.getAverageRatingByMovieId(movieId))
                .build();
    }

    // Thêm mới: Xóa mềm một đánh giá
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> softDeleteReview(@PathVariable Long reviewId) {
        reviewService.softDeleteReview(reviewId);
        return ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đánh giá với ID " + reviewId + " đã được xóa mềm")
                .build();
    }
}