package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.CinemaRoom;

import java.util.Optional;

public interface CinemaRoomRepository extends JpaRepository<CinemaRoom, Long> {
    Optional<CinemaRoom> findByCinemaRoomNameIgnoreCase(String name);
    boolean existsByCinemaRoomNameIgnoreCase(String name);
}
