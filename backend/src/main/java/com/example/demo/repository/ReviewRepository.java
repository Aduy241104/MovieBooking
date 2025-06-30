package com.example.demo.repository;

import com.example.demo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    long countByApproved(Boolean approved);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.approved = true")
    Double findAverageRatingOfApproved();

    // Thêm mới: Lấy danh sách đánh giá của một phim
    List<Review> findByMovieId(Long movieId);



    // Thêm mới: Đếm số đánh giá được duyệt của một phim
    long countByMovieIdAndApproved(Long movieId, Boolean approved);

    // Thêm mới: Tính trung bình sao của một phim
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie.id = :movieId AND r.approved = true")
    Double findAverageRatingByMovieId(Long movieId);
}