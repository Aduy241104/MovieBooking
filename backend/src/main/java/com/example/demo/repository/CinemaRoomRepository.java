package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.CinemaRoom;

import java.util.List;
import java.util.Optional;

public interface CinemaRoomRepository extends JpaRepository<CinemaRoom, Long> {

    /**
     * Finds a cinema room by name, ignoring case sensitivity.
     */
    Optional<CinemaRoom> findByCinemaRoomNameIgnoreCase(String name);

    /**
     * Checks if a cinema room with the given name exists (case insensitive).
     */
    boolean existsByCinemaRoomNameIgnoreCase(String name);

    /**
     * Retrieves all cinema rooms that are not marked as deleted (soft delete).
     */
    List<CinemaRoom> findByIsDeletedFalse();
}
