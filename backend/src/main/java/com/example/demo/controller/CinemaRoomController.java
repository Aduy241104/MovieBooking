//package com.example.demo.controller;
//
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import com.example.demo.DTO.request.RoomRequest;
//import com.example.demo.model.CinemaRoom;
//import com.example.demo.model.Seat;
//import com.example.demo.repository.CinemaRoomRepository;
//import com.example.demo.repository.SeatRepository;
//import com.example.demo.service.CinemaRoomService;
//
//import java.util.*;
//
//@RestController
//@RequestMapping("/api/rooms")
//@CrossOrigin
//@RequiredArgsConstructor
//public class CinemaRoomController {
//
//    private final CinemaRoomService roomService;
//    private final CinemaRoomRepository roomRepo;
//    private final SeatRepository seatRepo;
//
//    // Lấy danh sách tất cả phòng chiếu
//    @GetMapping
//    public List<CinemaRoom> getAllRooms() {
//        return roomService.getAllRooms();
//    }
//
//    // Lấy danh sách ghế trong phòng theo id
//    @GetMapping("/{id}/seats")
//    public ResponseEntity<List<Seat>> getSeats(@PathVariable Long id) {
//        List<Seat> seats = roomService.getSeatsByRoomId(id);
//        return ResponseEntity.ok(seats);
//    }
//
//    // Tạo mới một phòng chiếu kèm danh sách ghế
//    @PostMapping
//    public ResponseEntity<?> createRoom(@RequestBody RoomRequest request) {
//        try {
//            CinemaRoom room = roomService.createRoom(request); // Gọi service xử lý logic tạo
//            return ResponseEntity.ok(room); // Trả về phòng vừa tạo
//        } catch (Exception e) {
//            e.printStackTrace(); // In lỗi ra console
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", e.getMessage())); // Trả lỗi dưới dạng JSON
//        }
//    }
//
//    // Cập nhật phòng chiếu theo id
//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
//        try {
//            CinemaRoom room = roomService.updateRoom(id, request); // Gọi service cập nhật
//            return ResponseEntity.ok(room);
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build(); // Nếu không tìm thấy phòng
//        }
//    }
//
//    // Xóa một phòng chiếu
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
//        roomService.deleteRoom(id); // Gọi service xóa
//        return ResponseEntity.ok().build();
//    }
//
//    // Lấy thông tin chi tiết phòng chiếu + danh sách ghế + số hàng/cột
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getRoomWithSeats(@PathVariable Long id) {
//        Optional<CinemaRoom> optionalRoom = roomRepo.findById(id); // Tìm phòng theo ID
//
//        if (optionalRoom.isEmpty()) {
//            return ResponseEntity.notFound().build(); // Nếu không tìm thấy
//        }
//
//        CinemaRoom room = optionalRoom.get();
//        List<Seat> seats = seatRepo.findByCinemaRoom(room); // Lấy danh sách ghế theo phòng
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("id", room.getCinemaRoomId());
//        result.put("name", room.getCinemaRoomName());
//        result.put("rows", calculateMaxRow(seats)); // Tính số hàng từ danh sách ghế
//        result.put("cols", calculateMaxCol(seats)); // Tính số cột
//        result.put("seats", seats); // Gắn danh sách ghế
//
//        return ResponseEntity.ok(result); // Trả JSON chứa thông tin phòng + ghế
//    }
//
//    // Hàm tính số hàng tối đa từ danh sách ghế (theo ký tự 'A' -> 'Z')
//    private int calculateMaxRow(List<Seat> seats) {
//        return seats.stream()
//                .mapToInt(seat -> {
//                    String row = seat.getSeatRow().toUpperCase();
//                    if (!row.isEmpty()) {
//                        return row.charAt(0) - 'A'; // Ví dụ: 'A' -> 0, 'B' -> 1
//                    }
//                    return 0;
//                })
//                .max()
//                .orElse(0) + 1; // Cộng thêm 1 vì index bắt đầu từ 0
//    }
//
//    // Hàm tính số cột tối đa từ danh sách ghế (giả định seatCol là số dạng chuỗi)
//    private int calculateMaxCol(List<Seat> seats) {
//        return seats.stream()
//                .mapToInt(seat -> {
//                    try {
//                        return Integer.parseInt(seat.getSeatCol()); // seatCol là dạng "1", "2", ...
//                    } catch (NumberFormatException e) {
//                        return 0;
//                    }
//                })
//                .max()
//                .orElse(0) + 1;
//    }
//
//    // API riêng để lấy một phòng theo id (chỉ trả phòng, không trả danh sách ghế)
//    @GetMapping("/rooms/{id}")
//    public ResponseEntity<CinemaRoom> getRoomById(@PathVariable Long id) {
//        CinemaRoom room = roomService.getRoomById(id);
//        if (room == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(room);
//    }
//}
//
//
//
//
//
//
//
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

    // Get all cinema rooms
    @GetMapping
    public List<CinemaRoom> getAllRooms() {
        return roomService.getAllRooms();
    }

    // Get all seats in a room by room ID
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<Seat>> getSeats(@PathVariable Long id) {
        List<Seat> seats = roomService.getSeatsByRoomId(id);
        return ResponseEntity.ok(seats);
    }

    // Create a new cinema room with seat list
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

    // Update a cinema room by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
        try {
            CinemaRoom room = roomService.updateRoom(id, request); // Call service to update
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // If room not found
        }
    }

    // Delete a cinema room
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id); // Call service to delete
        return ResponseEntity.ok().build();
    }

    // Get cinema room details + seat list + number of rows/columns
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

    // Calculate maximum row index from seat list (based on 'A' -> 'Z')
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

    // Calculate maximum column index (assuming seatCol is a numeric string)
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

    // Separate API to get room by ID (returns only room, no seat list)
    @GetMapping("/rooms/{id}")
    public ResponseEntity<CinemaRoom> getRoomById(@PathVariable Long id) {
        CinemaRoom room = roomService.getRoomById(id);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(room);
    }
}
