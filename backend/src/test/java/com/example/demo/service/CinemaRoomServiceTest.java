////package com.example.demo.service;
////
////import org.junit.jupiter.api.Test;
////
////import static org.junit.jupiter.api.Assertions.*;
////
////class CinemaRoomServiceTest {
////
////    @Test
////    void getAllRooms() {
////    }
////
////    @Test
////    void getSeatsByRoomId() {
////    }
////
////    @Test
////    void createRoom() {
////    }
////
////    @Test
////    void updateRoom() {
////    }
////
////    @Test
////    void deleteRoom() {
////    }
////
////    @Test
////    void getRoomById() {
////    }
////
////    @Test
////    void getTotalCinemaRoom() {
////    }
////}
//package com.example.demo.service;
//
//import com.example.demo.DTO.request.RoomRequest;
//import com.example.demo.DTO.response.SeatResponse;
//import com.example.demo.exception.DuplicateNameException;
//import com.example.demo.exception.RoomNotFoundException;
//import com.example.demo.exception.SeatConversionException;
//import com.example.demo.model.*;
//import com.example.demo.repository.*;
//import com.example.demo.utils.SecurityUtils;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CinemaRoomServiceTest {
//
//    @InjectMocks
//    private CinemaRoomService cinemaRoomService;
//
//    @Mock
//    private CinemaRoomRepository roomRepo;
//
//    @Mock
//    private SeatRepository seatRepo;
//
//    @Mock
//    private SeatTypeRepository seatTypeRepo;
//
//    @Mock
//    private AccountRepository accountRepository;
//
//    @Mock
//    private ActivityLogService activityLogService;
//
//    @Mock
//    private NotificationService notificationService;
//
//    private CinemaRoom sampleRoom;
//
//    @BeforeEach
//    void setup() {
//        sampleRoom = new CinemaRoom();
//        sampleRoom.setCinemaRoomId(1L);
//        sampleRoom.setCinemaRoomName("Phòng 1");
//        sampleRoom.setIsDeleted(false);
//    }
//
//    @Test
//    void getAllRooms_shouldReturnList() {
//        List<CinemaRoom> rooms = Arrays.asList(sampleRoom);
//        when(roomRepo.findByIsDeletedFalse()).thenReturn(rooms);
//
//        List<CinemaRoom> result = cinemaRoomService.getAllRooms();
//        assertEquals(1, result.size());
//        verify(roomRepo).findByIsDeletedFalse();
//    }
//
//    @Test
//    void getSeatsByRoomId_shouldReturnSeats() {
//        List<Seat> seats = List.of(new Seat());
//        when(roomRepo.findById(1L)).thenReturn(Optional.of(sampleRoom));
//        when(seatRepo.findByCinemaRoom(sampleRoom)).thenReturn(seats);
//
//        List<Seat> result = cinemaRoomService.getSeatsByRoomId(1L);
//        assertEquals(1, result.size());
//    }
//
//    @Test
//    void createRoom_shouldSaveRoomAndSeats() {
//        RoomRequest request = new RoomRequest();
//        request.setName("Phòng 2");
//        request.setRows(2);
//        request.setCols(2);
//
//        SeatResponse seatDto = new SeatResponse();
//        seatDto.setSeatCol("1");
//        seatDto.setSeatRow(0);
//        seatDto.setSeatType("VIP");
//
//        request.setSeats(List.of(seatDto));
//
//        SeatType seatType = new SeatType();
//        seatType.setSeatTypeName("VIP");
//
//        when(roomRepo.existsByCinemaRoomNameIgnoreCase("Phòng 2")).thenReturn(false);
//        when(roomRepo.save(any())).thenReturn(sampleRoom);
//        when(seatTypeRepo.findBySeatTypeNameIgnoreCase("VIP")).thenReturn(Optional.of(seatType));
//        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(new Account()));
//        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of());
//
//        mockStatic(SecurityUtils.class);
//        when(SecurityUtils.getCurrentUsername()).thenReturn("1");
//
//        CinemaRoom room = cinemaRoomService.createRoom(request);
//        assertNotNull(room);
//    }
//
//    @Test
//    void updateRoom_shouldUpdateRoomAndSeats() {
//        RoomRequest request = new RoomRequest();
//        request.setName("Phòng mới");
//        SeatResponse seatDto = new SeatResponse();
//        seatDto.setSeatCol("1");
//        seatDto.setSeatRow(0);
//        seatDto.setSeatType("VIP");
//        request.setSeats(List.of(seatDto));
//
//        SeatType seatType = new SeatType();
//        seatType.setSeatTypeName("VIP");
//
//        when(roomRepo.findById(1L)).thenReturn(Optional.of(sampleRoom));
//        when(roomRepo.findByCinemaRoomNameIgnoreCase("Phòng mới")).thenReturn(Optional.empty());
//        when(seatTypeRepo.findBySeatTypeNameIgnoreCase("VIP")).thenReturn(Optional.of(seatType));
//        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(new Account()));
//        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of());
//        when(roomRepo.save(any())).thenReturn(sampleRoom);
//
//        mockStatic(SecurityUtils.class);
//        when(SecurityUtils.getCurrentUsername()).thenReturn("1");
//
//        CinemaRoom result = cinemaRoomService.updateRoom(1L, request);
//        assertEquals("Phòng mới", result.getCinemaRoomName());
//    }
//
//    @Test
//    void deleteRoom_shouldSoftDeleteRoom() {
//        when(roomRepo.findById(1L)).thenReturn(Optional.of(sampleRoom));
//        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(new Account()));
//        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of());
//        mockStatic(SecurityUtils.class);
//        when(SecurityUtils.getCurrentUsername()).thenReturn("1");
//
//        cinemaRoomService.deleteRoom(1L);
//        assertTrue(sampleRoom.getIsDeleted());
//    }
//
//    @Test
//    void getRoomById_shouldReturnRoom() {
//        when(roomRepo.findById(1L)).thenReturn(Optional.of(sampleRoom));
//        CinemaRoom result = cinemaRoomService.getRoomById(1L);
//        assertNotNull(result);
//    }
//
//    @Test
//    void getTotalCinemaRoom_shouldReturnCount() {
//        when(roomRepo.count()).thenReturn(10L);
//        assertEquals(10L, cinemaRoomService.getTotalCinemaRoom());
//    }
//
//    @Test
//    void createRoom_shouldThrowDuplicateNameException() {
//        when(roomRepo.existsByCinemaRoomNameIgnoreCase("Phòng 1")).thenReturn(true);
//        RoomRequest request = new RoomRequest();
//        request.setName("Phòng 1");
//
//        DuplicateNameException ex = assertThrows(DuplicateNameException.class, () -> {
//            cinemaRoomService.createRoom(request);
//        });
//        assertEquals("Tên phòng đã tồn tại.", ex.getMessage());
//    }
//
//    @Test
//    void updateRoom_shouldThrowRoomNotFound() {
//        when(roomRepo.findById(99L)).thenReturn(Optional.empty());
//        RoomRequest request = new RoomRequest();
//        RoomNotFoundException ex = assertThrows(RoomNotFoundException.class, () -> {
//            cinemaRoomService.updateRoom(99L, request);
//        });
//        assertTrue(ex.getMessage().contains("Không tìm thấy"));
//    }
//
//    @Test
//    void convertSeat_shouldThrowSeatConversionException() {
//        RoomRequest request = new RoomRequest();
//        request.setName("Phòng test");
//        request.setRows(1);
//        request.setCols(1);
//
//        SeatResponse seat = new SeatResponse();
//        seat.setSeatCol("1");
//        seat.setSeatRow(0);
//        seat.setSeatType("SaiLoai");
//
//        request.setSeats(List.of(seat));
//
//        when(roomRepo.existsByCinemaRoomNameIgnoreCase("Phòng test")).thenReturn(false);
//        when(roomRepo.save(any())).thenReturn(sampleRoom);
//        when(seatTypeRepo.findBySeatTypeNameIgnoreCase("SaiLoai")).thenReturn(Optional.empty());
//
//        // Sử dụng try-with-resources để tự động đóng static mock sau khi test
//        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
//            utilities.when(SecurityUtils::getCurrentUsername).thenReturn("1");
//
//            when(accountRepository.findById(anyLong())).thenReturn(Optional.of(new Account()));
//            when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of());
//
//            SeatConversionException ex = assertThrows(SeatConversionException.class, () -> {
//                cinemaRoomService.createRoom(request);
//            });
//            assertTrue(ex.getMessage().contains("Seat type không tồn tại"));
//        }
//    }
//}
package com.example.demo.service;

import com.example.demo.DTO.request.RoomRequest;
import com.example.demo.DTO.response.SeatResponse;
import com.example.demo.exception.DuplicateNameException;
import com.example.demo.exception.RoomNotFoundException;
import com.example.demo.exception.SeatConversionException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.utils.SecurityUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaRoomServiceTest {

    @InjectMocks
    private CinemaRoomService cinemaRoomService;

    @Mock
    private CinemaRoomRepository roomRepo;

    @Mock
    private SeatRepository seatRepo;

    @Mock
    private SeatTypeRepository seatTypeRepo;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private NotificationService notificationService;

    private CinemaRoom sampleRoom;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setup() {
        sampleRoom = new CinemaRoom();
        sampleRoom.setCinemaRoomId(1L);
        sampleRoom.setCinemaRoomName("Phòng 1");
        sampleRoom.setIsDeleted(false);

        // Mở static mock cho SecurityUtils
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUsername).thenReturn("1");
    }

    @AfterEach
    void tearDown() {
        // Đóng static mock sau mỗi test
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }

    @Test
    void getAllRooms_shouldReturnList() {
        List<CinemaRoom> rooms = Arrays.asList(sampleRoom);
        when(roomRepo.findByIsDeletedFalse()).thenReturn(rooms);

        List<CinemaRoom> result = cinemaRoomService.getAllRooms();
        assertEquals(1, result.size());
        verify(roomRepo).findByIsDeletedFalse();
    }

    @Test
    void getSeatsByRoomId_shouldReturnSeats() {
        List<Seat> seats = List.of(new Seat());
        when(roomRepo.findById(1L)).thenReturn(Optional.of(sampleRoom));
        when(seatRepo.findByCinemaRoom(sampleRoom)).thenReturn(seats);

        List<Seat> result = cinemaRoomService.getSeatsByRoomId(1L);
        assertEquals(1, result.size());
    }

    @Test
    void createRoom_shouldSaveRoomAndSeats() {
        RoomRequest request = new RoomRequest();
        request.setName("Phòng 2");
        request.setRows(2);
        request.setCols(2);

        SeatResponse seatDto = new SeatResponse();
        seatDto.setSeatCol("1");
        seatDto.setSeatRow(0);
        seatDto.setSeatType("VIP");

        request.setSeats(List.of(seatDto));

        SeatType seatType = new SeatType();
        seatType.setSeatTypeName("VIP");

        when(roomRepo.existsByCinemaRoomNameIgnoreCase("Phòng 2")).thenReturn(false);
        when(roomRepo.save(any())).thenReturn(sampleRoom);
        when(seatTypeRepo.findBySeatTypeNameIgnoreCase("VIP")).thenReturn(Optional.of(seatType));
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(new Account()));
        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of());

        CinemaRoom room = cinemaRoomService.createRoom(request);
        assertNotNull(room);
    }

    @Test
    void updateRoom_shouldUpdateRoomAndSeats() {
        RoomRequest request = new RoomRequest();
        request.setName("Phòng mới");
        SeatResponse seatDto = new SeatResponse();
        seatDto.setSeatCol("1");
        seatDto.setSeatRow(0);
        seatDto.setSeatType("VIP");
        request.setSeats(List.of(seatDto));

        SeatType seatType = new SeatType();
        seatType.setSeatTypeName("VIP");

        when(roomRepo.findById(1L)).thenReturn(Optional.of(sampleRoom));
        when(roomRepo.findByCinemaRoomNameIgnoreCase("Phòng mới")).thenReturn(Optional.empty());
        when(seatTypeRepo.findBySeatTypeNameIgnoreCase("VIP")).thenReturn(Optional.of(seatType));
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(new Account()));
        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of());
        when(roomRepo.save(any())).thenReturn(sampleRoom);

        CinemaRoom result = cinemaRoomService.updateRoom(1L, request);
        assertEquals("Phòng mới", result.getCinemaRoomName());
    }

    @Test
    void deleteRoom_shouldSoftDeleteRoom() {
        when(roomRepo.findById(1L)).thenReturn(Optional.of(sampleRoom));
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(new Account()));
        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of());

        cinemaRoomService.deleteRoom(1L);
        assertTrue(sampleRoom.getIsDeleted());
    }

    @Test
    void getRoomById_shouldReturnRoom() {
        when(roomRepo.findById(1L)).thenReturn(Optional.of(sampleRoom));
        CinemaRoom result = cinemaRoomService.getRoomById(1L);
        assertNotNull(result);
    }

    @Test
    void getTotalCinemaRoom_shouldReturnCount() {
        when(roomRepo.count()).thenReturn(10L);
        assertEquals(10L, cinemaRoomService.getTotalCinemaRoom());
    }

    @Test
    void createRoom_shouldThrowDuplicateNameException() {
        when(roomRepo.existsByCinemaRoomNameIgnoreCase("Phòng 1")).thenReturn(true);
        RoomRequest request = new RoomRequest();
        request.setName("Phòng 1");

        DuplicateNameException ex = assertThrows(DuplicateNameException.class, () -> {
            cinemaRoomService.createRoom(request);
        });
        assertEquals("Tên phòng đã tồn tại.", ex.getMessage());
    }

    @Test
    void updateRoom_shouldThrowRoomNotFound() {
        when(roomRepo.findById(99L)).thenReturn(Optional.empty());
        RoomRequest request = new RoomRequest();

        RoomNotFoundException ex = assertThrows(RoomNotFoundException.class, () -> {
            cinemaRoomService.updateRoom(99L, request);
        });
        assertTrue(ex.getMessage().contains("Không tìm thấy"));
    }

    @Test
    void convertSeat_shouldThrowSeatConversionException() {
        RoomRequest request = new RoomRequest();
        request.setName("Phòng test");
        request.setRows(1);
        request.setCols(1);

        SeatResponse seat = new SeatResponse();
        seat.setSeatCol("1");
        seat.setSeatRow(0);
        seat.setSeatType("SaiLoai");

        request.setSeats(List.of(seat));

        when(roomRepo.existsByCinemaRoomNameIgnoreCase("Phòng test")).thenReturn(false);
        when(roomRepo.save(any())).thenReturn(sampleRoom);
        when(seatTypeRepo.findBySeatTypeNameIgnoreCase("SaiLoai")).thenReturn(Optional.empty());

        SeatConversionException ex = assertThrows(SeatConversionException.class, () -> {
            cinemaRoomService.createRoom(request);
        });

        assertTrue(ex.getMessage().contains("Seat type không tồn tại"));
    }

}
