package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.response.SingleMovieDTO;
import com.example.demo.repository.MovieRepository;

@Service
public class MovieScheduleService {

    @Autowired
    MovieRepository movieRepository;

    public List<SingleMovieDTO> getNowShowingMovie(LocalDate currentDate) {
        List<SingleMovieDTO> result = movieRepository.findNowShowingMovies(currentDate);
        return result;
    }

    public List<SingleMovieDTO> getCommingSoonMovie(LocalDate currentDate) {
        List<SingleMovieDTO> result = movieRepository.findUpcomingMovies(currentDate);
        return result;
    }
}
