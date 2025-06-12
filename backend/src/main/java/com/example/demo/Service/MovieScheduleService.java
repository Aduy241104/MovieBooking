package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.response.SingleMovieDTO;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.TypeRepository;

@Service
public class MovieScheduleService {

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    TypeRepository typeRepository;

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
}
