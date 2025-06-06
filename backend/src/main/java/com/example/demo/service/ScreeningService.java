package com.example.demo.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.response.MovieScheduleDTO;
import com.example.demo.DTO.response.ShowTimeDTO;
import com.example.demo.model.Movie;
import com.example.demo.model.Screening;
import com.example.demo.repository.MovieTypeRepository;
import com.example.demo.repository.ScreeningRepository;

@Service
public class ScreeningService {

    @Autowired
    ScreeningRepository screeningRepository;

    @Autowired
    private MovieTypeRepository movieTypeRepository;

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
}
