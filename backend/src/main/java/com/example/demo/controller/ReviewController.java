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

    /**
     * Creates a new review based on the provided data.
     *
     * @param dto the data of the review to be created
     * @return an ApiResponse containing the created review details
     */
    @PostMapping("/add")
    public ApiResponse<ReviewResponseDTO> addReview(@RequestBody ReviewRequestDTO dto) {
        ReviewResponseDTO result = reviewService.addReview(dto);
        return ApiResponse.<ReviewResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Thêm đánh giá thành công")
                .result(result)
                .build();
    }

    /**
     * Updates an existing review by its ID.
     *
     * @param id  the ID of the review to be updated
     * @param dto the updated review data
     * @return an ApiResponse containing the updated review details
     */
    @PutMapping("/{id}")
    public ApiResponse<ReviewResponseDTO> updateReview(@PathVariable Long id, @RequestBody ReviewRequestDTO dto) {
        ReviewResponseDTO result = reviewService.updateReview(id, dto);
        return ApiResponse.<ReviewResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật đánh giá thành công")
                .result(result)
                .build();
    }

    /**
     * Permanently deletes a review by its ID.
     *
     * @param id the ID of the review to delete
     * @return an ApiResponse indicating the deletion status
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ApiResponse.<Void>builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message("Đánh giá đã được xóa")
                .build();
    }

    /**
     * Retrieves paginated reviews for a specific movie.
     *
     * @param movieId the ID of the movie
     * @param page    the page number (default is 0)
     * @param size    the page size (default is 7)
     * @return a paginated list of review DTOs
     */
    @GetMapping("/movie-aa/{movieId}/paged")
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

    // Lấy tất cả đánh giá của một phim
    @GetMapping("/movie/{movieId}")
    public ApiResponse<List<Review>> getReviewsByMovieId(@PathVariable Long movieId) {
        return ApiResponse.<List<Review>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách đánh giá của phim ID " + movieId)
                .result(reviewService.getReviewsByMovieId(movieId))
                .build();
    }

    // Đếm số đánh giá được duyệt của một phim
    @GetMapping("/movie/total-approved/{movieId}")
    public ApiResponse<Long> totalApprovedReviewsByMovieId(@PathVariable Long movieId) {
        return ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Số lượng đánh giá được duyệt của phim ID " + movieId)
                .result(reviewService.getTotalApprovedReviewsByMovieId(movieId))
                .build();
    }

    // Tính trung bình sao của một phim
    @GetMapping("/movie/average-rating/{movieId}")
    public ApiResponse<Double> averageRatingByMovieId(@PathVariable Long movieId) {
        return ApiResponse.<Double>builder()
                .status(HttpStatus.OK.value())
                .message("Trung bình điểm đánh giá được duyệt của phim ID " + movieId)
                .result(reviewService.getAverageRatingByMovieId(movieId))
                .build();
    }

    // Xóa mềm một đánh giá
    @DeleteMapping("/admin/{reviewId}")
    public ApiResponse<Void> softDeleteReview(@PathVariable Long reviewId) {
        reviewService.softDeleteReview(reviewId);
        return ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Đánh giá với ID " + reviewId + " đã được xóa mềm")
                .build();
    }

}