package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.DTO.response.SingleMovieDTO;
import com.example.demo.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByNameVNContainingIgnoreCase(String nameVN);

    @Query("""
                SELECT new com.example.demo.DTO.response.SingleMovieDTO(
                    m.id, m.nameVN, m.nameEN, m.duration, m.content,
                    m.fromDate, m.toDate, m.smallImage, m.largeImage,
                    m.trailer, COALESCE(AVG(r.rating), 0), null
                )
                FROM Movie m
                LEFT JOIN Review r ON r.movie.id = m.id
                WHERE LOWER(m.nameVN) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(m.nameEN) LIKE LOWER(CONCAT('%', :keyword, '%'))
                GROUP BY m.id, m.nameVN, m.nameEN, m.duration, m.content,
                         m.fromDate, m.toDate, m.smallImage, m.largeImage,
                         m.trailer
                ORDER BY m.id
            """)
    Page<SingleMovieDTO> searchMoviesWithRating(@Param("keyword") String keyword, Pageable pageable);
    
}
