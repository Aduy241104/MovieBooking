package com.example.demo.service;

import com.example.demo.repository.ReviewRepository;
import org.springframework.stereotype.Service;

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
        return reviewRepository.findAverageRatingOfApproved();
    }
}
