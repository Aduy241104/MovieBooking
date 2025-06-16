package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.response.MovieScheduleDTO;
import com.example.demo.DTO.response.SingleMovieDTO;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Movie;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.TypeRepository;

@Service
public class MovieScheduleService {

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    TypeRepository typeRepository;

    @Autowired
    ScreeningService screeningService;

    public List<SingleMovieDTO> getNowShowingMovie(LocalDate currentDate) {
        List<SingleMovieDTO> result = movieRepository.findNowShowingMovies(currentDate);

        for (SingleMovieDTO singleMovieDTO : result) {
            List<String> types = typeRepository.findTypeNamesByMovieId(singleMovieDTO.getId());
            singleMovieDTO.setTypes(types);
        }
        return result;
    }

    public List<SingleMovieDTO> getCommingSoonMovie(LocalDate currentDate) {
        List<SingleMovieDTO> result = movieRepository.findUpcomingMovies(currentDate);
        for (SingleMovieDTO singleMovieDTO : result) {
            List<String> types = typeRepository.findTypeNamesByMovieId(singleMovieDTO.getId());
            singleMovieDTO.setTypes(types);
        }
        return result;
    }


    public List<MovieScheduleDTO> getSchedule(LocalDate date) {
        List<MovieScheduleDTO> result = screeningService.getAllMovieScheduleByDate(date);
        return result;
    }

    public SingleMovieDTO getMovieDetail(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("movie not found"));

        List<String> types = typeRepository.findTypeNamesByMovieId(movie.getId());

        SingleMovieDTO singleMovieDTO = SingleMovieDTO.builder()
                .id(movie.getId())
                .nameVN(movie.getNameVN())
                .nameEN(movie.getNameEN())
                .duration(movie.getDuration())
                .content(movie.getContent())
                .fromDate(movie.getFromDate())
                .toDate(movie.getToDate())
                .smallImage(movie.getSmallImage())
                .largeImage(movie.getLargeImage())
                .trailer(movie.getTrailer())
                .ageLimit(movie.getAgeLimit())
                .types(types)
                .build();
        return singleMovieDTO;
    }

    public long getTotalNowShowingMovie() {
        LocalDate now = LocalDate.now();
        return movieRepository.fetchTotalNowShowingMovies(now);
    }
}
