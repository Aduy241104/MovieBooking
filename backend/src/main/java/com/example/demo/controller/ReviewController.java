package com.example.demo.controller;

import com.example.demo.DTO.request.ReviewRequestDTO;
import com.example.demo.DTO.response.MovieReviewWrapperDTO;
import com.example.demo.DTO.response.ReviewResponseDTO;
import com.example.demo.model.Review;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.service.ReviewService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {
    @Autowired
    private ReviewRepository reviewRepository;

    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review) {
        Review savedReview = reviewRepository.save(review);
        return ResponseEntity.ok(savedReview);
    }

    private final ReviewService reviewService;


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
}
