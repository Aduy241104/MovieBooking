package com.example.demo.repository;

import java.util.List;

import com.example.demo.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.MovieType;

public interface MovieTypeRepository extends JpaRepository<MovieType, Integer> {
    @Query("SELECT mt.type.name FROM MovieType mt WHERE mt.movie.id = :movieId")
    List<String> findTypeNamesByMovieId(@Param("movieId") Long movieId);

    @Query(value = """
            SELECT
                t.type_name AS name,
                COUNT(DISTINCT m.movie_id) AS count,
                COALESCE(SUM(bs.price_paid), 0) AS revenue
            FROM type t
            JOIN movie_type mt ON t.type_id = mt.type_id
            JOIN movie m ON mt.movie_id = m.movie_id
            LEFT JOIN screening s ON s.movie_id = m.movie_id
            LEFT JOIN booking b ON b.screening_id = s.screening_id AND b.booking_status = 'PAID'
            LEFT JOIN booked_seat bs ON bs.booking_id = b.booking_id
            GROUP BY t.type_name
            ORDER BY revenue DESC
            """, nativeQuery = true)
    List<Object[]> getMoviesByTypeRevenue();
    List<MovieType> findByMovie(Movie movie);
    void deleteByMovieId(Long movieId);
}