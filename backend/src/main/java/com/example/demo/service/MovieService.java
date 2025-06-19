package com.example.demo.service;

import com.example.demo.DTO.request.MovieRequest;
import com.example.demo.DTO.response.MovieResponse;
import com.example.demo.model.Movie;
import com.example.demo.model.MovieType;
import com.example.demo.model.Type;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.TypeRepository;
import com.example.demo.repository.MovieTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final TypeRepository typeRepository;
    private final MovieTypeRepository movieTypeRepository;

    private final Path uploadRoot = Paths.get("uploads");

    private String saveFile(MultipartFile file, String subfolder) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path dir = uploadRoot.resolve(subfolder);
        Files.createDirectories(dir);
        Path filepath = dir.resolve(filename);
        Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);
        return "/images/" + filename;
    }

    @Transactional
    public MovieResponse createMovie(MovieRequest request) throws IOException {
        Movie movie = new Movie();
        mapRequestToEntity(movie, request);

        if (request.getSmallImage() != null)
            movie.setSmallImage(saveFile(request.getSmallImage(), "images"));

        if (request.getLargeImage() != null)
            movie.setLargeImage(saveFile(request.getLargeImage(), "images"));

        if (request.getTrailerLink() != null && !request.getTrailerLink().isBlank())
            movie.setTrailer(request.getTrailerLink());

        movieRepository.save(movie);

        if (request.getTypeIds() != null) {
            List<Type> types = typeRepository.findAllByIdIn(request.getTypeIds());
            for (Type type : types) {
                movieTypeRepository.save(new MovieType(null, movie, type));
            }
        }

        return mapEntityToResponse(movie);
    }

    @Transactional
    public MovieResponse updateMovie(Long movieId, MovieRequest request) throws IOException {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));
        mapRequestToEntity(movie, request);

        if (request.getSmallImage() != null)
            movie.setSmallImage(saveFile(request.getSmallImage(), "images"));

        if (request.getLargeImage() != null)
            movie.setLargeImage(saveFile(request.getLargeImage(), "images"));

        if (request.getTrailerLink() != null && !request.getTrailerLink().isBlank())
            movie.setTrailer(request.getTrailerLink());

        movieRepository.save(movie);

        // Bước 1: Xóa các thể loại cũ
        movieTypeRepository.deleteByMovieId(movieId);
        movieRepository.flush(); // Đảm bảo DELETE thực thi ngay lập tức

        // Bước 2: Thêm lại thể loại mới
        if (request.getTypeIds() != null) {
            List<Type> types = typeRepository.findAllByIdIn(request.getTypeIds());
            for (Type type : types) {
                movieTypeRepository.save(new MovieType(null, movie, type));
            }
        }

        return mapEntityToResponse(movie);
    }

    public List<MovieResponse> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        return movies.stream().map(this::mapEntityToResponse).collect(Collectors.toList());
    }

    private void mapRequestToEntity(Movie movie, MovieRequest request) {
        movie.setNameVN(request.getNameVN());
        movie.setNameEN(request.getNameEN());
        movie.setContent(request.getContent());
        movie.setDuration(request.getDuration());
        movie.setFromDate(request.getFromDate());
        movie.setToDate(request.getToDate());
        movie.setDirector(request.getDirector());
        movie.setActor(request.getActor());
        movie.setMovieProductionCompany(request.getMovieProductionCompany());
        movie.setAgeLimit(request.getAgeLimit());
    }

    private MovieResponse mapEntityToResponse(Movie movie) {
        List<MovieType> movieTypes = movieTypeRepository.findAll()
                .stream()
                .filter(mt -> mt.getMovie().getId().equals(movie.getId()))
                .toList();

        List<String> typeNames = movieTypes.stream()
                .map(mt -> mt.getType().getName())
                .collect(Collectors.toList());

        List<Long> typeIds = movieTypes.stream()
                .map(mt -> mt.getType().getId().longValue())
                .collect(Collectors.toList());

        return new MovieResponse(
                movie.getId(),
                movie.getNameVN(),
                movie.getNameEN(),
                movie.getDuration(),
                movie.getContent(),
                movie.getFromDate(),
                movie.getToDate(),
                movie.getSmallImage(),
                movie.getLargeImage(),
                movie.getTrailer(),
                movie.getDirector(),
                movie.getActor(),
                movie.getMovieProductionCompany(),
                movie.getAgeLimit(),
                typeNames,
                typeIds
        );
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với id: " + id));
        return mapEntityToResponse(movie);
    }
}
