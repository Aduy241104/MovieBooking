package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.request.RoomRequest;
import com.example.demo.DTO.response.SeatResponse;
import com.example.demo.model.CinemaRoom;
import com.example.demo.model.Seat;
import com.example.demo.model.SeatType;
import com.example.demo.repository.CinemaRoomRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.SeatTypeRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CinemaRoomService {

    private final CinemaRoomRepository roomRepo;
    private final SeatRepository seatRepo;
    private final SeatTypeRepository seatTypeRepo;

    // Lấy tất cả phòng chiếu chưa bị xóa mềm
    public List<CinemaRoom> getAllRooms() {
        return roomRepo.findByIsDeletedFalse();
    }

    // Lấy danh sách ghế theo ID phòng
    public List<Seat> getSeatsByRoomId(Long roomId) {
        return roomRepo.findById(roomId)
                .map(seatRepo::findByCinemaRoom)
                .orElse(Collections.emptyList());
    }

    // Tạo phòng chiếu mới cùng danh sách ghế
    public CinemaRoom createRoom(RoomRequest request) {
        validateRoomNameUniqueness(request.getName());

        CinemaRoom room = new CinemaRoom();
        room.setCinemaRoomName(request.getName());
        room.setSeatQuantity(request.getRows() * request.getCols());
        room = roomRepo.save(room);

        List<Seat> seats = mapSeatResponsesToEntities(request.getSeats(), room);
        seatRepo.saveAll(seats);

        return room;
    }

    // Cập nhật phòng chiếu và ghế
    public CinemaRoom updateRoom(Long id, RoomRequest request) {
        CinemaRoom room = roomRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chiếu."));

        validateRoomNameConflict(id, request.getName());

        room.setCinemaRoomName(request.getName());
        room.setSeatQuantity(request.getSeats().size());
        room = roomRepo.save(room);

        // Xóa ghế cũ, tạo ghế mới
        seatRepo.deleteAll(seatRepo.findByCinemaRoom(room));
        List<Seat> newSeats = mapSeatResponsesToEntities(request.getSeats(), room);
        seatRepo.saveAll(newSeats);

        return room;
    }

    // Xóa mềm phòng chiếu
    public void deleteRoom(Long id) {
        CinemaRoom room = roomRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chiếu với ID: " + id));
        room.setIsDeleted(true);
        roomRepo.save(room);
    }

    // Lấy thông tin một phòng chiếu theo ID
    public CinemaRoom getRoomById(Long id) {
        return roomRepo.findById(id).orElse(null);
    }

    // ==== Các hàm phụ trợ ====

    // Kiểm tra trùng tên khi tạo phòng
    private void validateRoomNameUniqueness(String name) {
        if (roomRepo.existsByCinemaRoomNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tên phòng đã tồn tại.");
        }
    }

    // Kiểm tra trùng tên khi cập nhật phòng (trừ chính nó)
    private void validateRoomNameConflict(Long currentRoomId, String name) {
        Optional<CinemaRoom> existingRoom = roomRepo.findByCinemaRoomNameIgnoreCase(name);
        if (existingRoom.isPresent() && !existingRoom.get().getCinemaRoomId().equals(currentRoomId)) {
            throw new IllegalArgumentException("Tên phòng đã tồn tại.");
        }
    }

    // Chuyển danh sách DTO sang Entity
    private List<Seat> mapSeatResponsesToEntities(List<SeatResponse> seatResponses, CinemaRoom room) {
        List<Seat> seats = new ArrayList<>();
        for (SeatResponse dto : seatResponses) {
            seats.add(mapDtoToSeat(dto, room));
        }
        return seats;
    }

    // Chuyển từng DTO sang Seat entity
    private Seat mapDtoToSeat(SeatResponse dto, CinemaRoom room) {
        SeatType seatType = seatTypeRepo.findBySeatTypeNameIgnoreCase(dto.getSeatType())
                .orElseThrow(() -> new IllegalArgumentException("Seat type không tồn tại: " + dto.getSeatType()));

        Seat seat = new Seat();
        seat.setCinemaRoom(room);
        seat.setSeatCol(dto.getSeatCol());
        seat.setSeatRow(convertRowIndexToChar(dto.getSeatRow()));
        seat.setSeatStatus("available");
        seat.setSeatType(seatType);

        return seat;
    }

    // Chuyển số hàng (int) sang ký tự chữ cái (0 -> A, 1 -> B, ...)
    private String convertRowIndexToChar(int index) {
        return String.valueOf((char) ('A' + index));

}
    public Long getTotalCinemaRoom() {
        return roomRepo.count();
    }

}
