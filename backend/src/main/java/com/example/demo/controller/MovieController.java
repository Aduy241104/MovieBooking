package com.example.demo.controller;


import com.example.demo.DTO.request.MovieRequest;
import com.example.demo.DTO.response.MovieResponse;
import com.example.demo.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.http.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/public/movies")
@RequiredArgsConstructor
@CrossOrigin
public class MovieController {
    private final MovieService movieService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MovieResponse> addMovie( @ModelAttribute MovieRequest req) throws IOException {
        return ResponseEntity.ok(movieService.createMovie(req));
    }

    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id, @ModelAttribute MovieRequest req) throws IOException {
        return ResponseEntity.ok(movieService.updateMovie(id, req));
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAll() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
