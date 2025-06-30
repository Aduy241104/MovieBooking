package com.example.demo.controller;

import com.example.demo.DTO.response.MovieResponse;
import com.example.demo.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/public/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;


    @GetMapping("/getAll")
    public ResponseEntity<Page<MovieResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MovieResponse> movies = movieService.getAllMovies(page, size);
        return ResponseEntity.ok(movies);
    }
}