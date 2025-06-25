package com.example.demo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Screening;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    @Query("SELECT s FROM Screening s WHERE FUNCTION('DATE', s.showDateTime) = :date")
    List<Screening> findScreeningsByDate(@Param("date") LocalDate date);

    // Lấy các suất chiếu cho một phim cụ thể, còn hoạt động và trong tương lai hoặc hôm nay
    @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND s.isDeleted = false AND s.showDateTime >= :now ORDER BY s.showDateTime ASC")
    List<Screening> findActiveScreeningsForMovie(@Param("movieId") Long movieId, @Param("now") LocalDateTime now);
}