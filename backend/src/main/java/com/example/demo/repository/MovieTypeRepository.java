package com.example.demo.repository;

import java.util.List;

import com.example.demo.DTO.response.dashboard.MovieTypeRevenueResponse;
import com.example.demo.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.MovieType;

public interface MovieTypeRepository extends JpaRepository<MovieType, Integer> {
    @Query("SELECT mt.type.name FROM MovieType mt WHERE mt.movie.id = :movieId")
    List<String> findTypeNamesByMovieId(@Param("movieId") Long movieId);
    List<MovieType> findByMovie_Id(Long movieId);
    @Query(value = """
            SELECT

                    t.type_name,
                    (
                        SELECT COUNT(bs.booked_seat_id)
                        FROM booked_seat bs
                        JOIN booking b2 ON bs.booking_id = b2.booking_id
                        JOIN screening s2 ON b2.screening_id = s2.screening_id
                        JOIN movie_type mt2 ON mt2.movie_id = s2.movie_id
                        WHERE mt2.type_id = t.type_id AND b2.booking_status = 'PAID'
                    ),
                    (
                        SELECT COALESCE(SUM(b3.total_amount), 0)
                        FROM booking b3
                        JOIN screening s3 ON b3.screening_id = s3.screening_id
                        JOIN movie_type mt3 ON mt3.movie_id = s3.movie_id
                        WHERE mt3.type_id = t.type_id AND b3.booking_status = 'PAID'
                    )
            FROM type t
            ORDER BY 3 DESC
            """, nativeQuery = true)
    List<MovieTypeRevenueResponse.MovieTypeRevenue> getMoviesByTypeRevenue();

    List<MovieType> findByMovie(Movie movie);

    void deleteByMovieId(Long movieId);

    List<MovieType> findByType_NameIgnoreCaseAndMovie_IsDeletedFalse(String typeName);
}