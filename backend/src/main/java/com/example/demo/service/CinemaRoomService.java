package com.example.demo.service;


import com.example.demo.DTO.request.RoomRequest;
import com.example.demo.DTO.response.SeatResponse;
import com.example.demo.exception.DuplicateNameException;
import com.example.demo.exception.RoomNotFoundException;
import com.example.demo.exception.SeatConversionException;
import com.example.demo.exception.UnauthorizedException;

import com.example.demo.exception.AppException;
import com.example.demo.model.*;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CinemaRoomRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.SeatTypeRepository;
import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CinemaRoomService {

    @Autowired
    private CinemaRoomRepository roomRepo;
    @Autowired
    private SeatRepository seatRepo;
    @Autowired
    private SeatTypeRepository seatTypeRepo;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ActivityLogService activityLogService;
    @Autowired
    private NotificationService notificationService;

    /**
     * Get all cinema rooms that are not soft-deleted.
     *
     * @return List of active cinema rooms.
     */
    public List<CinemaRoom> getAllRooms() {
        return roomRepo.findByIsDeletedFalse();
    }

    /**
     * Get all seats in a cinema room by room ID.
     *
     * @param roomId ID of the cinema room.
     * @return List of seats or empty list if room not found.
     */
    public List<Seat> getSeatsByRoomId(Long roomId) {
        return roomRepo.findById(roomId)
                .map(seatRepo::findByCinemaRoom)
                .orElse(Collections.emptyList());
    }

    /**
     * Create a new cinema room and associated seats.
     *
     * @param request RoomRequest with room and seat data.
     * @return The created CinemaRoom entity.
     */
    public CinemaRoom createRoom(RoomRequest request) {
        validateRoomNameUniqueness(request.getName());

        CinemaRoom room = new CinemaRoom();
        room.setCinemaRoomName(request.getName());
        room.setSeatQuantity(request.getRows() * request.getCols());
        room = roomRepo.save(room);

        List<Seat> seats = mapSeatResponsesToEntities(request.getSeats(), room);
        seatRepo.saveAll(seats);


        // Log activity and notification for room creation

        setLogAndNotification(room, "TẠO MỚI",
                "Tạo phòng chiếu mới",
                "Tạo mới phòng chiếu",
                " đã tạo mới phòng chiếu: ");

        return room;
    }

    /**
     * Update an existing cinema room and its seats.
     *
     * @param id      Room ID to update.
     * @param request RoomRequest with new data.
     * @return Updated CinemaRoom entity.
     */
    public CinemaRoom updateRoom(Long id, RoomRequest request) {
        CinemaRoom room = roomRepo.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Không tìm thấy phòng chiếu."));

        validateRoomNameConflict(id, request.getName());

        room.setCinemaRoomName(request.getName());
        room.setSeatQuantity(request.getSeats().size());
        room = roomRepo.save(room);

        seatRepo.deleteAll(seatRepo.findByCinemaRoom(room));
        List<Seat> newSeats = mapSeatResponsesToEntities(request.getSeats(), room);
        seatRepo.saveAll(newSeats);


        setLogAndNotification(room, "CẬP NHẬT",
                "Cập nhật thông tin phòng chiếu",
                "Cập nhật thông tin phòng chiếu",
                " đã cập nhật thông tin phòng chiếu: ");

        return room;
    }

    /**
     * Soft delete a cinema room (mark isDeleted = true).
     *
     * @param id ID of the room to delete.
     */
    public void deleteRoom(Long id) {
        CinemaRoom room = roomRepo.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Không tìm thấy phòng chiếu với ID: " + id));
        room.setIsDeleted(true);
        roomRepo.save(room);


        setLogAndNotification(room, "XOÁ",
                "Xoá phòng chiếu",
                "Xoá phòng chiếu",
                " đã xoá phòng chiếu: ");
    }

    /**
     * Get a specific cinema room by ID.
     *
     * @param id Room ID.
     * @return CinemaRoom or null if not found.
     */
    public CinemaRoom getRoomById(Long id) {
        return roomRepo.findById(id).orElse(null);
    }

    /**
     * Validates if a room name already exists.
     *
     * @param name Room name to check.
     * @throws DuplicateNameException if name exists.
     */
    private void validateRoomNameUniqueness(String name) {
        if (roomRepo.existsByCinemaRoomNameIgnoreCase(name)) {
            throw new DuplicateNameException("Tên phòng đã tồn tại.");
        }
    }

    /**
     * Validates if the updated room name conflicts with another room.
     *
     * @param currentRoomId Current room ID being updated.
     * @param name          New room name to check.
     * @throws DuplicateNameException if conflict exists.
     */
    private void validateRoomNameConflict(Long currentRoomId, String name) {
        Optional<CinemaRoom> existingRoom = roomRepo.findByCinemaRoomNameIgnoreCase(name);
        if (existingRoom.isPresent() && !existingRoom.get().getCinemaRoomId().equals(currentRoomId)) {
            throw new DuplicateNameException("Tên phòng đã tồn tại.");
        }
    }

    /**
     * Converts a list of SeatResponse DTOs to Seat entities for a given room.
     *
     * @param seatResponses List of SeatResponse.
     * @param room          CinemaRoom the seats belong to.
     * @return List of Seat entities.
     */
    private List<Seat> mapSeatResponsesToEntities(List<SeatResponse> seatResponses, CinemaRoom room) {
        List<Seat> seats = new ArrayList<>();
        for (SeatResponse dto : seatResponses) {
            seats.add(mapDtoToSeat(dto, room));
        }
        return seats;
    }

    /**
     * Converts a SeatResponse DTO to a Seat entity.
     *
     * @param dto  SeatResponse object.
     * @param room The associated cinema room.
     * @return Seat entity.
     */
    private Seat mapDtoToSeat(SeatResponse dto, CinemaRoom room) {
        SeatType seatType = seatTypeRepo.findBySeatTypeNameIgnoreCase(dto.getSeatType())
                .orElseThrow(() -> new SeatConversionException("Seat type không tồn tại: " + dto.getSeatType()));

        Seat seat = new Seat();
        seat.setCinemaRoom(room);
        seat.setSeatCol(dto.getSeatCol());
        seat.setSeatRow(convertRowIndexToChar(dto.getSeatRow()));
        seat.setSeatStatus("available");
        seat.setSeatType(seatType);

        return seat;
    }

    /**
     * Converts seat row index (0,1,2...) to alphabetical (A,B,C,...).
     *
     * @param index Integer index.
     * @return Corresponding row letter.
     */
    private String convertRowIndexToChar(int index) {
        return String.valueOf((char) ('A' + index));
    }

    /**
     * Count total number of cinema rooms.
     *
     * @return Total number of rooms in DB.
     */
    public Long getTotalCinemaRoom() {
        return roomRepo.count();
    }


    /**
     * Sets log and sends notification for room actions.
     *
     * @param room       Room affected.
     * @param action     Action string (CREATE/UPDATE/DELETE).
     * @param description Description for activity log.
     * @param title      Notification title.
     * @param content    Notification content with actor and room name.
     */
    private void setLogAndNotification(CinemaRoom room, String action, String description,
                                       String title, String content) {
        // Get current user ID from security context
        String loginUserId = SecurityUtils.getCurrentUsername();

        if(loginUserId == null || loginUserId.isEmpty()) {
            throw new AppException("User not logged in");
        }
        Account user = accountRepository.findById(Long.valueOf(loginUserId))
                .orElseThrow(() -> new AppException("Current user not found"));
        // Log activity

        activityLogService.log(
                user.getEmail(),
                action,
                "PHÒNG CHIẾU",
                room.getCinemaRoomName(),
                description
        );

        // Send notification to all admins

        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        for (Account admin : adminAccounts) {
            if (!admin.getAccountId().equals(user.getAccountId())) {
                notificationService.notify(
                        admin,
                        title,
                        user.getFullName() + content + room.getCinemaRoomName(),
                        "SYSTEM"
                );
            }
        }
    }
}
