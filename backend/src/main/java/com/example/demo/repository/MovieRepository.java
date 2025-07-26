package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.demo.DTO.response.SingleMovieDTO;

import com.example.demo.DTO.response.dashboard.TopMovieByRevenueResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Tìm phim theo tên tiếng Việt (gần đúng, không phân biệt hoa thường)
    List<Movie> findByNameVNContainingIgnoreCaseAndIsDeletedFalse(String nameVN);

    // Tìm phim theo tên tiếng Anh (gần đúng, không phân biệt hoa thường)
    List<Movie> findByNameENContainingIgnoreCaseAndIsDeletedFalse(String nameEN);

    // Tìm phim trong khoảng ngày chiếu
    List<Movie> findByFromDateLessThanEqualAndToDateGreaterThanEqualAndIsDeletedFalse(LocalDate from, LocalDate to);

    // Tìm tất cả phim chưa bị xóa
    List<Movie> findByIsDeletedFalse();

    @Query("""
                SELECT new com.example.demo.DTO.response.SingleMovieDTO(
                    m.id, m.nameVN, m.nameEN, m.duration, m.content,
                    m.fromDate, m.toDate, m.smallImage, m.largeImage,
                    m.trailer, m.ageLimit,m.director, m.movieProductionCompany,COALESCE(AVG(r.rating), 0), null
                )
                FROM Movie m
                LEFT JOIN Review r ON r.movie.id = m.id
                WHERE LOWER(m.nameVN) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(m.nameEN) LIKE LOWER(CONCAT('%', :keyword, '%'))
                GROUP BY m.id, m.nameVN, m.nameEN, m.duration, m.content,
                         m.fromDate, m.toDate, m.smallImage, m.largeImage,
                         m.trailer, m.ageLimit
                ORDER BY m.id
            """)
    Page<SingleMovieDTO> searchMoviesWithRating(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
                SELECT new com.example.demo.DTO.response.SingleMovieDTO(
                    m.id, m.nameVN, m.nameEN, m.duration, m.content,
                    m.fromDate, m.toDate, m.smallImage, m.largeImage,
                    m.trailer, m.ageLimit,m.director, m.movieProductionCompany,COALESCE(AVG(r.rating), 0), null
                )
                FROM Movie m
                LEFT JOIN Review r ON r.movie.id = m.id
                WHERE m.fromDate <= :currentDate AND m.toDate >= :currentDate AND m.isDeleted = false
                GROUP BY m.id,m.nameVN, m.nameEN, m.duration, m.content,
                         m.fromDate, m.toDate, m.smallImage, m.largeImage, m.trailer, m.ageLimit
                ORDER BY m.id
            """)
    List<SingleMovieDTO> findNowShowingMovies(@Param("currentDate") LocalDate currentDate);

    @Query("""
                SELECT new com.example.demo.DTO.response.SingleMovieDTO(
                    m.id,m.nameVN, m.nameEN, m.duration, m.content,
                    m.fromDate, m.toDate, m.smallImage, m.largeImage,
                    m.trailer, m.ageLimit,m.director, m.movieProductionCompany,COALESCE(AVG(r.rating), 0), null
                )
                FROM Movie m
                LEFT JOIN Review r ON r.movie.id = m.id
                WHERE m.fromDate > :currentDate  AND m.isDeleted = false
                GROUP BY m.id, m.nameVN, m.nameEN, m.duration, m.content,
                         m.fromDate, m.toDate, m.smallImage, m.largeImage, m.trailer, m.ageLimit
                ORDER BY m.fromDate
            """)
    List<SingleMovieDTO> findUpcomingMovies(@Param("currentDate") LocalDate currentDate);

    @Query("""
                SELECT COUNT(m)
                FROM Movie m
                WHERE m.fromDate <= :currentDate AND m.toDate >= :currentDate
            """)
    Long fetchTotalNowShowingMovies(@Param("currentDate") LocalDate currentDate);

    @Query(value = """
            SELECT
                m.movie_name_en,
                COALESCE(STRING_AGG(DISTINCT t.type_name, ', '), '') AS genres,
                COALESCE(AVG(r.rating), 0.0) AS avg_rating,
                (
                    SELECT COUNT(bs.booked_seat_id)
                    FROM booking b2
                    JOIN booked_seat bs ON bs.booking_id = b2.booking_id
                    JOIN screening s2 ON b2.screening_id = s2.screening_id
                    WHERE s2.movie_id = m.movie_id AND b2.booking_status = 'PAID'
                ) AS ticket_sold,
                (
                    SELECT COALESCE(SUM(b1.total_amount), 0)
                    FROM booking b1
                    JOIN screening s1 ON b1.screening_id = s1.screening_id
                    WHERE s1.movie_id = m.movie_id AND b1.booking_status = 'PAID'
                ) AS revenue,
                m.small_image
            FROM movie m
            LEFT JOIN movie_type mt ON mt.movie_id = m.movie_id
            LEFT JOIN type t ON t.type_id = mt.type_id
            LEFT JOIN review r ON r.movie_id = m.movie_id AND r.is_approved = true
            WHERE m.is_deleted = false
            GROUP BY m.movie_id, m.movie_name_en, m.small_image
            ORDER BY revenue DESC
            LIMIT 5
            """, nativeQuery = true)
    List<TopMovieByRevenueResponse> getTopMoviesByRevenue();

    Optional<Movie> findByNameVNAndIsDeletedFalse(String nameVN);

    Optional<Movie> findByNameENAndIsDeletedFalse(String nameEN);
}
