package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.demo.DTO.response.SingleMovieDTO;
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
    long fetchTotalNowShowingMovies(@Param("currentDate") LocalDate currentDate);

    @Query(value = """
                SELECT
                    m.movie_name_en AS movie_title,
                    STRING_AGG(DISTINCT t.type_name, ', ') AS genres,
                    COALESCE(avg_reviews.avg_rating, 0) AS avg_rating,
                    COALESCE(ticket_stats.tickets_sold, 0) AS tickets_sold,
                    COALESCE(ticket_stats.revenue, 0) AS revenue,
                    m.small_image AS small_image
                FROM movie m
                LEFT JOIN movie_type mt ON m.movie_id = mt.movie_id
                LEFT JOIN type t ON mt.type_id = t.type_id

                -- Subquery tính vé và doanh thu chỉ booking PAID
                LEFT JOIN (
                    SELECT m2.movie_id,
                            COUNT(bs.booked_seat_id) AS tickets_sold,
                            SUM(bs.price_paid) AS revenue
                    FROM movie m2
                    JOIN screening s2 ON s2.movie_id = m2.movie_id
                    JOIN booking b2 ON b2.screening_id = s2.screening_id AND b2.booking_status = 'PAID'
                    JOIN booked_seat bs ON bs.booking_id = b2.booking_id
                    GROUP BY m2.movie_id
                ) ticket_stats ON m.movie_id = ticket_stats.movie_id

                -- Subquery tính rating trung bình
                LEFT JOIN (
                    SELECT r.movie_id, AVG(r.rating) AS avg_rating
                    FROM review r
                    WHERE r.is_approved = true
                    GROUP BY r.movie_id
                ) avg_reviews ON m.movie_id = avg_reviews.movie_id

                GROUP BY m.movie_id, m.movie_name_en, avg_reviews.avg_rating, ticket_stats.tickets_sold, ticket_stats.revenue
                ORDER BY revenue DESC
                LIMIT 5
            """, nativeQuery = true)
    List<Object[]> getTopMoviesByRevenue();
    Optional<Movie> findByNameVNAndIsDeletedFalse(String nameVN);
    Optional<Movie> findByNameENAndIsDeletedFalse(String nameEN);
}
