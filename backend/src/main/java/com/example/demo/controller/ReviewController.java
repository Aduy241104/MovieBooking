package com.example.demo.controller;

import com.example.demo.DTO.request.ReviewRequestDTO;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.MovieReviewWrapperDTO;
import com.example.demo.DTO.response.PaginatedResponseDTO;
import com.example.demo.DTO.response.ReviewResponseDTO;
import com.example.demo.model.Review;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.service.ReviewService;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/reviews")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

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

    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review) {
        Review savedReview = reviewRepository.save(review);
        return ResponseEntity.ok(savedReview);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<MovieReviewWrapperDTO>> getWrappedReviews(@PathVariable Long movieId) {
        return ResponseEntity.ok(reviewService.getWrappedReviewsByMovieId(movieId));
    }

    @PostMapping("/add")
    public ResponseEntity<ReviewResponseDTO> addReview(@RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.ok(reviewService.addReview(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> updateReview(@PathVariable Integer id, @RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.ok(reviewService.updateReview(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/movie/{movieId}/paged")
    public ResponseEntity<PaginatedResponseDTO<ReviewResponseDTO>> getPagedReviewsByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size) {

        Page<ReviewResponseDTO> resultPage = reviewService.getPaginatedReviewsByMovieId(movieId, page, size);

        PaginatedResponseDTO<ReviewResponseDTO> response = new PaginatedResponseDTO<>(
                resultPage.getNumber(), // pageNumber
                resultPage.getSize(), // pageSize
                resultPage.getTotalPages(), // totalPages
                resultPage.getContent() // content list
        );

        return ResponseEntity.ok(response);
    }
}