package com.example.demo.service;

import com.example.demo.DTO.response.MovieResponse;
import com.example.demo.model.Movie;
import com.example.demo.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public Page<MovieResponse> getAllMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> moviePage = movieRepository.findAll(pageable);

        return moviePage.map(this::convertToResponse);
    }

    private MovieResponse convertToResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setId(movie.getId());
        response.setNameVN(movie.getNameVN());
        response.setNameEN(movie.getNameEN());
        response.setDuration(movie.getDuration());
        response.setContent(movie.getContent());
        response.setFromDate(movie.getFromDate());
        response.setToDate(movie.getToDate());
        response.setSmallImage(movie.getSmallImage());
        response.setLargeImage(movie.getLargeImage());
        response.setTrailer(movie.getTrailer());
        response.setDirector(movie.getDirector());
        response.setMovieProductionCompany(movie.getMovieProductionCompany());
        response.setActor(movie.getActor());
        response.setAgeLimit(movie.getAgeLimit());
        // You might want to add logic to calculate averageRating if needed
        return response;
    }
}