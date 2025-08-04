package com.example.demo.repository;

import com.example.demo.model.Review;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Get paginated list of reviews by movie ID
    Page<Review> findByMovieId(Long movieId, Pageable pageable);

    // Get all reviews for a movie without pagination
    List<Review> findByMovieId(Long movieId);

    // Get all reviews for a movie ordered by review date descending
    List<Review> findByMovieIdOrderByReviewDateDesc(Long movieId);

    // Count total number of reviews by approval status
    long countByApproved(Boolean approved);

    // Check if a specific user has already reviewed a movie (used for validation)
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.account.accountId = :accountId AND r.movie.id = :movieId")
    boolean hasReviewed(@Param("accountId") Long accountId, @Param("movieId") Long movieId);

    // Calculate average rating of all approved reviews
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.approved = true")
    Double findAverageRatingOfApproved();


    // ADMIN : Tính trung bình sao của một phim
    // Count the number of approved reviews for a specific movie
    long countByMovieIdAndApproved(Long movieId, Boolean approved);

    // Calculate the average rating for a specific movie (only approved reviews)

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie.id = :movieId AND r.approved = true")
    Double findAverageRatingByMovieId(Long movieId);

}
