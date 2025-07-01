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

import com.example.demo.DTO.response.MovieScheduleDTO;
import com.example.demo.DTO.response.ShowTimeDTO;
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
                                                        .map(s -> new ShowTimeDTO(s.getId(),
                                                                        s.getShowDateTime().toLocalTime(),
                                                                        s.getShowDateTime().toLocalTime().plusMinutes(
                                                                                        movie.getDuration())))
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
                                s.getFareType() != null ? s.getFareType().getMovieFormat() : "N/A" // Lấy MovieFormat từ FareType
                        ), Collectors.toList())
                ));

        // Sắp xếp thời gian trong mỗi ngày
        groupedSchedules.forEach((date, times) -> times.sort(Comparator.comparing(ScreeningScheduleResponseDTO.ScreeningTimeDTO::getTime)));

        // Sắp xếp các ngày
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
            if (seat.getSeatStatus() != null && seat.getSeatStatus().equalsIgnoreCase("Unavailable")) { // Giả sử có trạng thái Unavailable trong DB
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
}
