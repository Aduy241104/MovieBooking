package com.example.demo.repository;

import com.example.demo.model.Review;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Page<Review> findByMovieId(Long movieId, Pageable pageable);
    List<Review> findByMovieId(Long movieId);
     long countByApproved(Boolean approved);

     @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.account.id = :accountId AND r.movie.id = :movieId")
boolean hasReviewed(@Param("accountId") Long accountId, @Param("movieId") Long movieId);


    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.approved = true")
    Double findAverageRatingOfApproved();


}
