package com.example.demo.service;

import com.example.demo.model.Review;
import com.example.demo.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public long getTotalApprovedReviews() {
        return reviewRepository.countByApproved(true);
    }

    public Double findAverageRatingOfApproved() {
        Double average = reviewRepository.findAverageRatingOfApproved();
        return average != null ? average : 0.0;
    }

    //Lấy tất cả đánh giá của một phim
    public List<Review> getReviewsByMovieId(Long movieId) {
        return reviewRepository.findByMovieId(movieId);
    }

    // Đếm số đánh giá được duyệt của một phim
    public long getTotalApprovedReviewsByMovieId(Long movieId) {
        return reviewRepository.countByMovieIdAndApproved(movieId, true);
    }

    //Tính trung bình sao của một phim
    public Double getAverageRatingByMovieId(Long movieId) {
        Double average = reviewRepository.findAverageRatingByMovieId(movieId);
        return average != null ? average : 0.0;
    }
    // Xóa mềm một đánh giá
    public void softDeleteReview(Long reviewId) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            review.setIsDeleted(true);
            reviewRepository.save(review);
        });
    }
}