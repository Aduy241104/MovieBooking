package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.MovieType;

public interface MovieTypeRepository extends JpaRepository<MovieType, Integer> {
    @Query("SELECT mt.type.name FROM MovieType mt WHERE mt.movie.id = :movieId")
    List<String> findTypeNamesByMovieId(@Param("movieId") Long movieId);

}