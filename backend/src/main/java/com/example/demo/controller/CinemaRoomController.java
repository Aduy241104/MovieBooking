package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.DTO.request.RoomRequest;
import com.example.demo.model.CinemaRoom;
import com.example.demo.model.Seat;
import com.example.demo.repository.CinemaRoomRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.service.CinemaRoomService;

import java.util.*;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin
@RequiredArgsConstructor
public class CinemaRoomController {

    private final CinemaRoomService roomService;
    private final CinemaRoomRepository roomRepo;
    private final SeatRepository seatRepo;

    /**
     * Get all cinema rooms.
     */
    @GetMapping
    public List<CinemaRoom> getAllRooms() {
        return roomService.getAllRooms();
    }

    /**
     * Get all seats in a room by room ID.
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<Seat>> getSeats(@PathVariable Long id) {
        List<Seat> seats = roomService.getSeatsByRoomId(id);
        return ResponseEntity.ok(seats);
    }

    /**
     * Create a new cinema room with seats.
     */
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody RoomRequest request) {
        try {
            CinemaRoom room = roomService.createRoom(request); // Call service to handle creation logic
            return ResponseEntity.ok(room); // Return the created room
        } catch (Exception e) {
            e.printStackTrace(); // Print error to console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage())); // Return error as JSON
        }
    }

    /**
     * Update a cinema room by ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
        try {
            CinemaRoom room = roomService.updateRoom(id, request); // Call service to update
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // If room not found
        }
    }

    /**
     * Soft delete a cinema room by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id); // Call service to delete
        return ResponseEntity.ok().build();
    }

    /**
     * Get room details with seats, number of rows and columns.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomWithSeats(@PathVariable Long id) {
        Optional<CinemaRoom> optionalRoom = roomRepo.findById(id); // Find room by ID

        if (optionalRoom.isEmpty()) {
            return ResponseEntity.notFound().build(); // If not found
        }

        CinemaRoom room = optionalRoom.get();
        List<Seat> seats = seatRepo.findByCinemaRoom(room); // Get seat list for the room

        Map<String, Object> result = new HashMap<>();
        result.put("id", room.getCinemaRoomId());
        result.put("name", room.getCinemaRoomName());
        result.put("rows", calculateMaxRow(seats)); // Calculate number of rows
        result.put("cols", calculateMaxCol(seats)); // Calculate number of columns
        result.put("seats", seats); // Add seat list

        return ResponseEntity.ok(result); // Return JSON with room and seat info
    }

    /**
     * Calculate number of rows in the seat list.
     * Based on alphabetic row names: A = 1, B = 2, etc.
     */
    private int calculateMaxRow(List<Seat> seats) {
        return seats.stream()
                .mapToInt(seat -> {
                    String row = seat.getSeatRow().toUpperCase();
                    if (!row.isEmpty()) {
                        return row.charAt(0) - 'A'; // Example: 'A' -> 0, 'B' -> 1
                    }
                    return 0;
                })
                .max()
                .orElse(0) + 1; // Add 1 since index starts from 0
    }

    /**
     * Calculate number of columns based on numeric seatCol values.
     */
    private int calculateMaxCol(List<Seat> seats) {
        return seats.stream()
                .mapToInt(seat -> {
                    try {
                        return Integer.parseInt(seat.getSeatCol()); // seatCol is like "1", "2", ...
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0) + 1;
    }

    /**
     * Get cinema room only (without seats) by ID.
     */
    @GetMapping("/rooms/{id}")
    public ResponseEntity<CinemaRoom> getRoomById(@PathVariable Long id) {
        CinemaRoom room = roomService.getRoomById(id);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(room);
    }
}
