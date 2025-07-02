package com.example.demo.repository;

import com.example.demo.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ChatbotScreeningRepository extends JpaRepository<Screening, Long> {

        // Lịch chiếu theo ngày
        @Query("SELECT s FROM Screening s WHERE DATE(s.showDateTime) = :date ORDER BY s.showDateTime")
        List<Screening> findByDate(@Param("date") LocalDate date);

        // Lịch chiếu theo phim và ngày
        @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND DATE(s.showDateTime) = :date ORDER BY s.showDateTime")
        List<Screening> findByMovieAndDate(@Param("movieId") Long movieId, @Param("date") LocalDate date);

        // Lịch chiếu theo phim (tên phim)
        @Query("SELECT s FROM Screening s WHERE (LOWER(s.movie.nameVN) LIKE LOWER(CONCAT('%', :movieName, '%')) " +
                        "OR LOWER(s.movie.nameEN) LIKE LOWER(CONCAT('%', :movieName, '%'))) " +
                        "AND s.showDateTime >= :currentDateTime ORDER BY s.showDateTime")
        List<Screening> findByMovieName(@Param("movieName") String movieName,
                        @Param("currentDateTime") LocalDateTime currentDateTime);

        // Suất chiếu trong khoảng thời gian
        @Query("SELECT s FROM Screening s WHERE s.showDateTime BETWEEN :startTime AND :endTime ORDER BY s.showDateTime")
        List<Screening> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        // Suất chiếu hôm nay
        @Query(value = "SELECT * FROM screening s WHERE s.show_date_time::date = CURRENT_DATE AND s.show_date_time > :currentDateTime ORDER BY s.show_date_time", nativeQuery = true)
        List<Screening> findTodayScreenings(@Param("currentDateTime") LocalDateTime currentDateTime);

        // Suất chiếu ngày mai
        @Query("SELECT s FROM Screening s WHERE DATE(s.showDateTime) = :tomorrow ORDER BY s.showDateTime")
        List<Screening> findTomorrowScreenings(@Param("tomorrow") LocalDate tomorrow);

        // Suất chiếu cuối tuần (thứ 7 và chủ nhật)
        @Query(value = "SELECT * FROM screening s WHERE EXTRACT(ISODOW FROM s.show_date_time) IN (6, 7) AND s.show_date_time >= :currentDateTime ORDER BY s.show_date_time", nativeQuery = true)
        List<Screening> findWeekendScreenings(@Param("currentDateTime") LocalDateTime currentDateTime);

        // Suất chiếu theo thời gian trong ngày (sáng, chiều, tối)
        @Query("SELECT s FROM Screening s WHERE DATE(s.showDateTime) = :date " +
                        "AND TIME(s.showDateTime) BETWEEN :startTime AND :endTime ORDER BY s.showDateTime")
        List<Screening> findByDateAndTimeRange(@Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        // Suất chiếu sớm nhất của phim
        @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId " +
                        "AND s.showDateTime >= :currentDateTime ORDER BY s.showDateTime LIMIT 1")
        Screening findEarliestScreeningByMovie(@Param("movieId") Long movieId,
                        @Param("currentDateTime") LocalDateTime currentDateTime);

        // Lịch chiếu theo keyword (LIKE, ignore-case, cả tên tiếng Việt và tiếng Anh)
        @Query(value = "SELECT s.* FROM screening s JOIN movie m ON m.movie_id = s.movie_id " +
                        "WHERE (LOWER(m.movie_name_vn) LIKE CONCAT('%', LOWER(:keyword), '%') " +
                        "OR LOWER(m.movie_name_en) LIKE CONCAT('%', LOWER(:keyword), '%')) " +
                        "AND s.show_date_time >= :fromDate ORDER BY s.show_date_time", nativeQuery = true)
        List<Screening> findScreeningsByMovieKeyword(@Param("keyword") String keyword,
                        @Param("fromDate") LocalDateTime fromDate);
}
