package com.example.demo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Screening;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    // Get screenings by exact date (ignoring time)
    @Query("SELECT s FROM Screening s WHERE FUNCTION('DATE', s.showDateTime) = :date")
    List<Screening> findScreeningsByDate(@Param("date") LocalDate date);

    // Booking: Get upcoming or ongoing screenings for a specific movie (not deleted)
    @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND s.isDeleted = false AND s.showDateTime >= :now ORDER BY s.showDateTime ASC")
    List<Screening> findActiveScreeningsForMovie(@Param("movieId") Long movieId, @Param("now") LocalDateTime now);

    // ADMIN: Get all screenings by date that are not soft-deleted
    @Query("SELECT s FROM Screening s WHERE FUNCTION('DATE', s.showDateTime) = :date AND s.isDeleted = false")
    List<Screening> findActiveScreeningsByDate(@Param("date") LocalDate date);

    // ADMIN: Get all screenings that are not soft-deleted
    @Query("SELECT s FROM Screening s WHERE s.isDeleted = false")
    List<Screening> findAllActive();

    // ADMIN: Check for overlapping screenings in the same cinema room
    @Query("SELECT s FROM Screening s WHERE s.cinemaRoom.cinemaRoomId = :cinemaRoomId AND s.isDeleted = false " +
            "AND s.showDateTime < :endTime AND :startTime < FUNCTION('TIMESTAMPADD', MINUTE, s.movie.duration, s.showDateTime)")
    List<Screening> findOverlappingScreenings(@Param("cinemaRoomId") Long cinemaRoomId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);
}
