package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.example.demo.DTO.response.booking.ScreeningScheduleResponseDTO;
import com.example.demo.DTO.response.booking.SeatStatusDTO;
import com.example.demo.exception.AppException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.exception.InternalServerException;
import com.example.demo.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.request.ScreeningRequest;
import com.example.demo.DTO.response.MovieScheduleDTO;
import com.example.demo.DTO.response.ShowTimeDTO;
import static com.example.demo.service.BookingService.BOOKING_EXPIRATION_MINUTES;
@Service
public class ScreeningService {

    @Autowired
    ScreeningRepository screeningRepository;

    @Autowired
    private MovieTypeRepository movieTypeRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private SeatRepository seatRepository;

    // dùng cho add/update screening
    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;

    // dùng cho add/update screening
    @Autowired
    private FareTypeRepository fareTypeRepository;

    // Use for user logging
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private BookedSeatRepository bookedSeatRepository;


    
    public List<MovieScheduleDTO> getAllMovieScheduleByDate(LocalDate date) {
        List<Screening> listScreening = screeningRepository.findScreeningsByDate(date);

        Map<Movie, List<Screening>> groupedByMovie = listScreening.stream()
                .collect(Collectors.groupingBy(Screening::getMovie));

        List<MovieScheduleDTO> result = groupedByMovie.entrySet().stream()
                .map(entry -> {
                    Movie movie = entry.getKey();

                    List<String> typesName = movieTypeRepository
                            .findTypeNamesByMovieId(movie.getId());

                    List<ShowTimeDTO> showTimes = entry.getValue().stream()
                            .map(s -> new ShowTimeDTO(
                                    s.getId(),
                                    s.getShowDateTime().toLocalTime(),
                                    s.getShowDateTime().toLocalTime().plusMinutes(movie.getDuration())
                            ))
                            .sorted(Comparator.comparing(ShowTimeDTO::getShowTime))
                            .collect(Collectors.toList());

                    return MovieScheduleDTO.builder()
                            .movie(movie)
                            .types(typesName)
                            .showTime(showTimes)
                            .build();

                })
                .collect(Collectors.toList());
        return result;
    }

    //Booking
    public ScreeningScheduleResponseDTO getSchedulesForMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phim với ID: " + movieId));

        List<Screening> screenings = screeningRepository.findActiveScreeningsForMovie(movieId, LocalDateTime.now().minusMinutes(15)); // Cho phép trễ 15p

        Map<LocalDate, List<ScreeningScheduleResponseDTO.ScreeningTimeDTO>> groupedSchedules = screenings.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getShowDateTime().toLocalDate(),
                        Collectors.mapping(s -> new ScreeningScheduleResponseDTO.ScreeningTimeDTO(
                                s.getId(),
                                s.getShowDateTime().toLocalTime(),
                                s.getCinemaRoom(),
                                s.getFareType(),
                                s.getFareType() != null ? s.getFareType().getMovieFormat() : "N/A"
                        ), Collectors.toList())
                ));

        groupedSchedules.forEach((date, times) -> times.sort(Comparator.comparing(ScreeningScheduleResponseDTO.ScreeningTimeDTO::getTime)));
        Map<LocalDate, List<ScreeningScheduleResponseDTO.ScreeningTimeDTO>> sortedGroupedSchedules = new TreeMap<>(groupedSchedules);

        return new ScreeningScheduleResponseDTO(movie, sortedGroupedSchedules);
    }


    public List<SeatStatusDTO> getSeatStatusForScreening(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy suất chiếu với ID: " + screeningId));
        CinemaRoom cinemaRoom = screening.getCinemaRoom();
        if (cinemaRoom == null) {
            throw new InternalServerException("Lỗi dữ liệu hệ thống: Suất chiếu không có thông tin phòng.", null);
        }

        List<Seat> allSeatsInRoom = seatRepository.findByCinemaRoom(cinemaRoom);
        List<BookedSeat> bookedSeats = bookedSeatRepository.findByScreeningId(screeningId);

        // Logic tạo Map giữ nguyên, nó đã rất tốt
        Map<Long, Booking> seatBookingMap = bookedSeats.stream()
                .filter(bs -> bs.getSeat() != null && bs.getBooking() != null)
                .collect(Collectors.toMap(
                        bs -> bs.getSeat().getSeatId(),
                        BookedSeat::getBooking,
                        (existingBooking, newBooking) -> {
                            if ("PAID".equals(existingBooking.getBookingStatus())) return existingBooking;
                            if ("PAID".equals(newBooking.getBookingStatus())) return newBooking;
                            if ("RESERVED".equals(existingBooking.getBookingStatus())) return existingBooking;
                            if ("RESERVED".equals(newBooking.getBookingStatus())) return newBooking;
                            if ("PENDING_PAYMENT".equals(existingBooking.getBookingStatus())) return existingBooking;
                            if ("PENDING_PAYMENT".equals(newBooking.getBookingStatus())) return newBooking;
                            return existingBooking;
                        }
                ));

        return allSeatsInRoom.stream().map(seat -> {
            String status = "Available";
            LocalDateTime expiresAt = null;

            Booking booking = seatBookingMap.get(seat.getSeatId());
            if (booking != null) {

                // <<< THAY ĐỔI LOGIC NẰM Ở ĐÂY >>>
                switch (booking.getBookingStatus()) {
                    case "PAID":
                    case "RESERVED":
                        status = "Booked";
                        break;
                    case "PENDING_PAYMENT":
                        // Tính toán thời gian hết hạn
                        LocalDateTime expirationTime = booking.getBookingTime().plusMinutes(BOOKING_EXPIRATION_MINUTES);

                        // Kiểm tra xem đã hết hạn hay chưa
                        if (expirationTime.isBefore(LocalDateTime.now())) {
                            // Mặc dù trong DB vẫn là PENDING, nhưng về mặt logic nó đã hết hạn.
                            // Coi như ghế này đã trống.
                            status = "Available";
                        } else {
                            // Nếu chưa hết hạn, mới hiển thị là Pending
                            status = "Pending";
                            expiresAt = expirationTime; // Gán thời gian hết hạn để trả về cho frontend
                        }
                        break;
                    default: // EXPIRED, FAILED, CANCELLED và các trạng thái khác
                        status = "Available";
                        break;
                }
            }

            if ("Unavailable".equalsIgnoreCase(seat.getSeatStatus())) {
                status = "Unavailable";
            }

            SeatType seatType = seat.getSeatType();
            return new SeatStatusDTO(
                    seat.getSeatId(),
                    seat.getSeatRow(),
                    seat.getSeatCol(),
                    seatType != null ? seatType.getSeatTypeName() : "Standard",
                    seatType != null ? seatType.getSeatTypePrice() : BigDecimal.ZERO,
                    status,
                    seatType != null ? seatType.getSeatTypeId() : null,
                    expiresAt
            );
        }).collect(Collectors.toList());
    }


    //  Thêm lịch chiếu mới
    public Screening addScreening(ScreeningRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));

        LocalDateTime startTime = request.getShowDateTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

        List<Screening> overlapping = screeningRepository.findOverlappingScreenings(
                request.getCinemaRoomId(), startTime, endTime);
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Lịch chiếu bị trùng với lịch chiếu hiện có trong cùng phòng chiếu");
        }

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setCinemaRoom(cinemaRoomRepository.findById(request.getCinemaRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu")));
        screening.setFareType(fareTypeRepository.findById(request.getFareTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại vé")));
        screening.setShowDateTime(startTime);
        screening.setIsDeleted(false);

        // Log and notify about the new screening creation
        setLogAndNotification(
                screening.getId(),
                "TẠO MỚI",
                "Tạo mới lịch chiếu cho phim: " + movie.getNameVN() + " tại phòng chiếu: "
                        + screening.getCinemaRoom().getCinemaRoomName(),
                "Tạo mới lịch chiếu",
                " vừa tạo mới lịch chiếu cho phim: " + movie.getNameVN() + " tại phòng chiếu: "
                        + screening.getCinemaRoom().getCinemaRoomName()
        );
        return screeningRepository.save(screening);
    }

    //  Cập nhật lịch chiếu
    public Screening updateScreening(Long id, ScreeningRequest request) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch chiếu"));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));

        LocalDateTime startTime = request.getShowDateTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

        List<Screening> overlapping = screeningRepository.findOverlappingScreenings(
                request.getCinemaRoomId(), startTime, endTime);
        overlapping.removeIf(s -> s.getId().equals(id));
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Lịch chiếu cập nhật bị trùng với lịch chiếu hiện có trong cùng phòng chiếu");
        }

        screening.setMovie(movie);
        screening.setCinemaRoom(cinemaRoomRepository.findById(request.getCinemaRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu")));
        screening.setFareType(fareTypeRepository.findById(request.getFareTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại vé")));
        screening.setShowDateTime(startTime);

        // Log and notify about the updated screening
        setLogAndNotification(
                screening.getId(),
                "CẬP NHẬT",
                "Cập nhật lịch chiếu cho phim: " + movie.getNameVN() + " tại phòng chiếu: "
                        + screening.getCinemaRoom().getCinemaRoomName(),
                "Tạo mới lịch chiếu",
                " vừa cập nhật lịch chiếu cho phim: " + movie.getNameVN() + " tại phòng chiếu: "
                + screening.getCinemaRoom().getCinemaRoomName()
        );

        return screeningRepository.save(screening);
    }

    //  Xoá mềm lịch chiếu
    public void softDeleteScreening(Long id) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch chiếu"));
        screening.setIsDeleted(true);
        screeningRepository.save(screening);

        // Log and notify about the screening deletion
        setLogAndNotification(
                screening.getId(),
                "XOÁ",
                "Xoá lịch chiếu của phim: " + screening.getMovie().getNameVN() + " tại phòng chiếu: "
                        + screening.getCinemaRoom().getCinemaRoomName(),
                "Xoá lịch chiếu",
                " vừa xoá lịch chiếu của phim: " + screening.getMovie().getNameVN() + " tại phòng chiếu: "
                + screening.getCinemaRoom().getCinemaRoomName()
        );
    }

    //  Lấy tất cả lịch chiếu chưa xoá
    public List<Screening> getAllActiveScreenings() {
        return screeningRepository.findAllActive();
    }

    /**
     * Sets log and sends notification for screening actions.
     */
    private void setLogAndNotification(Long screeningId, String action, String description,
                                       String title, String content) {
        // Check if the screening exists
        Screening currentScreening = getPromotionOrThrow(screeningId);
        // Get the current logged-in user
        String loginUserId = SecurityUtils.getCurrentUsername();
        if(loginUserId == null || loginUserId.isEmpty()) {
            throw new AppException("User not logged in!");
        }
        Account editorAccount = getAccountOrThrow(Long.valueOf(loginUserId));
        // Log the activity
        activityLogService.log(
                editorAccount.getEmail(),
                action,
                "LỊCH CHIẾU",
                currentScreening.getMovie().getNameVN(),
                description
        );
        // Find all admin accounts to notify
        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        if(adminAccounts.isEmpty()) {
            throw new AppException("No admin accounts found to notify");
        }
        // Send notifications to all admin accounts except the editor
        for (Account admin : adminAccounts) {
            if (!admin.getAccountId().equals(editorAccount.getAccountId())) {
                notificationService.notify(
                        admin,
                        title,
                        editorAccount.getFullName() + content + currentScreening.getMovie().getNameVN(),
                        "SYSTEM"
                );
            }
        }
    }

    /**
     * Find screening by id, or throw AppException("Screening not found").
     */
    public Screening getPromotionOrThrow(Long screeningId) {
        return screeningRepository.findById(screeningId)
                .orElseThrow(() -> new AppException("Screening not found"));
    }

    /**
     * Find account by id, or throw AppException("Account not found").
     */
    public Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException("Account not found"));
    }
}
