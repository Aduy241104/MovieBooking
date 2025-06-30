package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.request.ScreeningRequest;
import com.example.demo.DTO.response.MovieScheduleDTO;
import com.example.demo.DTO.response.ShowTimeDTO;
import com.example.demo.model.CinemaRoom;
import com.example.demo.model.FareType;
import com.example.demo.model.Movie;
import com.example.demo.model.Screening;
import com.example.demo.repository.CinemaRoomRepository;
import com.example.demo.repository.FareTypeRepository;
import com.example.demo.repository.MovieRepository;
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
    private CinemaRoomRepository cinemaRoomRepository;

    @Autowired
    private FareTypeRepository fareTypeRepository;

    public List<MovieScheduleDTO> getAllMovieScheduleByDate(LocalDate date) {
        List<Screening> listScreening = screeningRepository.findScreeningsByDate(date);

        Map<Movie, List<Screening>> groupedByMovie = listScreening.stream()
                .collect(Collectors.groupingBy(Screening::getMovie));

        List<MovieScheduleDTO> result = groupedByMovie.entrySet().stream()
                .map(entry -> {
                    Movie movie = entry.getKey();

                    List<String> typesName = movieTypeRepository.findTypeNamesByMovieId(movie.getId());

                    List<ShowTimeDTO> showTimes = entry.getValue().stream()
                            .map(s -> new ShowTimeDTO(s.getId(), s.getShowDateTime().toLocalTime()))
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

    public Screening addScreening(ScreeningRequest request) {
        // Lấy thông tin phim để tính thời gian kết thúc
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));

        // Tính thời gian kết thúc
        LocalDateTime startTime = request.getShowDateTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

        // Kiểm tra lịch chiếu chồng lấn
        List<Screening> overlapping = screeningRepository.findOverlappingScreenings(
                request.getCinemaRoomId(), startTime, endTime);
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Lịch chiếu bị trùng với lịch chiếu hiện có trong cùng phòng chiếu");
        }

        // Tạo lịch chiếu mới
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

    public Screening updateScreening(Long id, ScreeningRequest request) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch chiếu"));

        // Lấy thông tin phim để tính thời gian kết thúc
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));

        // Tính thời gian kết thúc
        LocalDateTime startTime = request.getShowDateTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

        // Kiểm tra lịch chiếu chồng lấn (loại trừ lịch chiếu hiện tại)
        List<Screening> overlapping = screeningRepository.findOverlappingScreenings(
                request.getCinemaRoomId(), startTime, endTime);
        overlapping.removeIf(s -> s.getId().equals(id)); // Loại trừ lịch chiếu đang cập nhật
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Lịch chiếu cập nhật bị trùng với lịch chiếu hiện có trong cùng phòng chiếu");
        }

        // Cập nhật lịch chiếu
        screening.setMovie(movie);
        screening.setCinemaRoom(cinemaRoomRepository.findById(request.getCinemaRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu")));
        screening.setFareType(fareTypeRepository.findById(request.getFareTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại vé")));
        screening.setShowDateTime(startTime);
        return screeningRepository.save(screening);
    }

    public void softDeleteScreening(Long id) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch chiếu"));
        screening.setIsDeleted(true);
        screeningRepository.save(screening);
    }

    public List<Screening> getAllActiveScreenings() {
        return screeningRepository.findAllActive();
    }
}