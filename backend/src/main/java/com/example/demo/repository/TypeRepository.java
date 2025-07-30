package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Type;

@Repository
public interface TypeRepository extends JpaRepository<Type, Integer> {

    /**
     * Finds the names of all types associated with a given movie ID.
     * Joins the MovieType relation to get the corresponding Type names.
     */
    @Query("SELECT t.name FROM MovieType mt JOIN mt.type t WHERE mt.movie.id = :movieId")
    List<String> findTypeNamesByMovieId(@Param("movieId") Long movieId);

    /**
     * Checks if a type with the given name already exists.
     */
    boolean existsByName(String name);

    /**
     * Returns all types that have not been soft-deleted.
     */
    List<Type> findByIsDeletedFalse();

    /**
     * Finds all types whose IDs are in the provided list.
     */
    List<Type> findAllByIdIn(List<Long> ids);

    /**
     * Checks if a type with the same name exists but with a different ID.
     * Useful to prevent name duplication during updates.
     */
    boolean existsByNameAndIdNot(String name, Integer id);
}