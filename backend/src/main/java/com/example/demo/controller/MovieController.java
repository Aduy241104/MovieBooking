package com.example.demo.controller;

import com.example.demo.DTO.request.MovieRequest;
import com.example.demo.DTO.response.MovieResponse;
import com.example.demo.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@CrossOrigin
public class MovieController {
    private final MovieService movieService;

    /**
     * Create a new movie.
     * Accepts multipart/form-data, including fields like image, trailer, and other movie info.
     *
     * @param req The MovieRequest object from the form.
     * @return The created movie's response.
     * @throws IOException if file processing fails.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MovieResponse> addMovie(@ModelAttribute MovieRequest req) throws IOException {
        return ResponseEntity.ok(movieService.createMovie(req));
    }

    /**
     * Update an existing movie by its ID.
     * Accepts multipart/form-data with updated image, trailer, or details.
     *
     * @param id The ID of the movie to update.
     * @param req The updated MovieRequest data.
     * @return The updated movie's response.
     * @throws IOException if file processing fails.
     */
    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id, @ModelAttribute MovieRequest req) throws IOException {
        return ResponseEntity.ok(movieService.updateMovie(id, req));
    }

    /**
     * Get a list of all movies that are not deleted.
     *
     * @return List of MovieResponse.
     */
    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAll() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    /**
     * Get detailed information of a movie by its ID.
     *
     * @param id The ID of the movie.
     * @return MovieResponse with detailed movie info.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    /**
     * Soft delete a movie by setting isDeleted = true.
     *
     * @param id The ID of the movie to delete.
     * @return 204 No Content if successful.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
