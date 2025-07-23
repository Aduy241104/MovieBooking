package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Type;

@Repository
public interface TypeRepository extends JpaRepository<Type, Integer> {

    @Query("SELECT t.name FROM MovieType mt JOIN mt.type t WHERE mt.movie.id = :movieId")
    List<String> findTypeNamesByMovieId(@Param("movieId") Long movieId);
    boolean existsByName(String name);
    List<Type> findByIsDeletedFalse();
    List<Type> findAllByIdIn(List<Long> ids);
    // Kiểm tra tên trùng với ID khác (dùng khi update)
    boolean existsByNameAndIdNot(String name, Integer id);
}