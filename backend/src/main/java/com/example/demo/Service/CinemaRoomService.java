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
@RequiredArgsConstructor // Tự động inject các repository thông qua constructor
public class CinemaRoomService {

    private final CinemaRoomRepository roomRepo;
    private final SeatRepository seatRepo;
    private final SeatTypeRepository seatTypeRepo;

    // Lấy danh sách tất cả các phòng chiếu
//    public List<CinemaRoom> getAllRooms() {
//        return roomRepo.findAll();
//    }
    public List<CinemaRoom> getAllRooms() {
        return roomRepo.findByIsDeletedFalse();
    }

    // Lấy danh sách ghế theo ID phòng chiếu
    public List<Seat> getSeatsByRoomId(Long roomId) {
        Optional<CinemaRoom> room = roomRepo.findById(roomId);
        if (room.isEmpty()) {
            return Collections.emptyList(); // Nếu không có phòng thì trả về danh sách rỗng
        }

        List<Seat> seats = seatRepo.findByCinemaRoom(room.get());

        // ✅ Ghi log thông tin ghế để debug hoặc kiểm tra dữ liệu
        System.out.println("Danh sách ghế của phòng ID: " + roomId);
        for (Seat seat : seats) {
            System.out.println("Ghế: row = " + seat.getSeatRow() +
                               ", col = " + seat.getSeatCol() +
                               ", type = " + (seat.getSeatType() != null ? seat.getSeatType().getSeatTypeName() : "null") +
                               ", price = " + (seat.getSeatType() != null ? seat.getSeatType().getSeatTypePrice() : "null"));
        }

        return seats;
    }

    // Tạo mới một phòng chiếu kèm theo ghế
    public CinemaRoom createRoom(RoomRequest request) {
        // Kiểm tra trùng tên phòng (không phân biệt hoa thường)
        if (roomRepo.existsByCinemaRoomNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Tên phòng đã tồn tại.");
        }

        // Tạo đối tượng phòng chiếu và lưu vào DB
        CinemaRoom room = new CinemaRoom();
        room.setCinemaRoomName(request.getName());
        room.setSeatQuantity(request.getRows() * request.getCols());
        room = roomRepo.save(room); // lưu để lấy ID phòng

        // Tạo danh sách ghế từ request và gán vào phòng
        List<Seat> seats = new ArrayList<>();
        for (SeatResponse dto : request.getSeats()) {
            seats.add(mapDtoToEntity(dto, room)); // Chuyển từng SeatResponse thành Seat
        }
        seatRepo.saveAll(seats); // Lưu tất cả ghế vào DB

        return room;
    }

    // Cập nhật phòng chiếu và danh sách ghế theo ID
    public CinemaRoom updateRoom(Long id, RoomRequest request) {
        // Tìm phòng theo ID, nếu không có thì ném lỗi
        CinemaRoom room = roomRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Kiểm tra trùng tên phòng với phòng khác
        Optional<CinemaRoom> sameNameRoom = roomRepo.findByCinemaRoomNameIgnoreCase(request.getName());
        if (sameNameRoom.isPresent() && !sameNameRoom.get().getCinemaRoomId().equals(id)) {
            throw new IllegalArgumentException("Tên phòng đã tồn tại.");
        }

        // Cập nhật thông tin phòng
        room.setCinemaRoomName(request.getName());
        room.setSeatQuantity(request.getSeats().size());
        room = roomRepo.save(room);

        final CinemaRoom finalRoom = room; // dùng để truyền vào lambda

        // Xoá toàn bộ ghế cũ của phòng
        List<Seat> oldSeats = seatRepo.findByCinemaRoom(room);
        seatRepo.deleteAll(oldSeats);

        // Tạo danh sách ghế mới từ dữ liệu gửi lên
        List<Seat> seats = request.getSeats().stream()
                .map(dto -> mapDtoToEntity(dto, finalRoom)) // chuyển từng SeatResponse sang Seat
                .toList();
        seatRepo.saveAll(seats); // lưu lại danh sách ghế mới

        return room;
    }

    // Xoá phòng chiếu theo ID (bao gồm cả ghế)
//    public void deleteRoom(Long id) {
//        CinemaRoom room = roomRepo.findById(id).orElseThrow(); // tìm phòng
//        seatRepo.deleteAll(seatRepo.findByCinemaRoom(room)); // xoá hết ghế thuộc phòng
//        roomRepo.deleteById(id); // xoá phòng
//    }

    public void deleteRoom(Long id) {
        CinemaRoom room = roomRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chiếu với ID: " + id));

        room.setIsDeleted(true); // Đánh dấu là đã xóa
        roomRepo.save(room);     // Lưu lại thay đổi
    }

    // Lấy phòng chiếu theo ID (dùng cho API /rooms/rooms/{id})
    public CinemaRoom getRoomById(Long id) {
        return roomRepo.findById(id).orElse(null);
    }

    // Hàm chuyển đổi từ SeatResponse (DTO) sang Seat (Entity)
    private Seat mapDtoToEntity(SeatResponse dto, CinemaRoom room) {
        Seat seat = new Seat();
        seat.setCinemaRoom(room); // Gán phòng chứa ghế
        seat.setSeatCol(dto.getSeatCol()); // Số cột (dạng chuỗi số)
        seat.setSeatRow(Character.toString((char) ('A' + dto.getSeatRow()))); // chuyển số hàng thành chữ cái: 0 -> A, 1 -> B,...
        seat.setSeatStatus("available"); // trạng thái mặc định là có sẵn

        // Tìm SeatType theo tên, nếu không có thì ném lỗi
        SeatType seatType = seatTypeRepo.findBySeatTypeNameIgnoreCase(dto.getSeatType())
                .orElseThrow(() -> new IllegalArgumentException("Seat type không tồn tại: " + dto.getSeatType()));
        seat.setSeatType(seatType); // gán loại ghế

        return seat;
    }
}
