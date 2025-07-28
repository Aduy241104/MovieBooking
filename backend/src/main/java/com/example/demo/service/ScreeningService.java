package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.example.demo.DTO.response.booking.ScreeningScheduleResponseDTO;
import com.example.demo.DTO.response.booking.SeatStatusDTO;
import com.example.demo.model.*;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.request.ScreeningRequest;
import com.example.demo.DTO.response.MovieScheduleDTO;
import com.example.demo.DTO.response.ShowTimeDTO;
import com.example.demo.repository.CinemaRoomRepository;
import com.example.demo.repository.FareTypeRepository;
import com.example.demo.repository.MovieTypeRepository;
import com.example.demo.repository.ScreeningRepository;

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
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));

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
                .orElseThrow(() -> new RuntimeException("Screening not found with id: " + screeningId));

        CinemaRoom cinemaRoom = screening.getCinemaRoom();
        if (cinemaRoom == null) {
            throw new RuntimeException("Cinema room not found for screening: " + screeningId);
        }

        List<Seat> allSeatsInRoom = seatRepository.findByCinemaRoom(cinemaRoom);
        Set<Long> bookedSeatIds = seatRepository.findBookedSeatIdsByScreeningId(screeningId);

        return allSeatsInRoom.stream().map(seat -> {
            String status = bookedSeatIds.contains(seat.getSeatId()) ? "Booked" : "Available";
            if (seat.getSeatStatus() != null && seat.getSeatStatus().equalsIgnoreCase("Unavailable")) {
                status = "Unavailable";
            }
            SeatType seatType = seat.getSeatType();
            return new SeatStatusDTO(
                    seat.getSeatId(),
                    seat.getSeatRow(),
                    seat.getSeatCol(),
                    seatType != null ? seatType.getSeatTypeName() : "Standard",
                    seatType != null ? seatType.getSeatTypePrice() : java.math.BigDecimal.ZERO,
                    status,
                    seatType != null ? seatType.getSeatTypeId() : null
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
        return screeningRepository.save(screening);
    }

    //  Xoá mềm lịch chiếu
    public void softDeleteScreening(Long id) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch chiếu"));
        screening.setIsDeleted(true);
        screeningRepository.save(screening);
    }

    //  Lấy tất cả lịch chiếu chưa xoá
    public List<Screening> getAllActiveScreenings() {
        return screeningRepository.findAllActive();
    }
}
