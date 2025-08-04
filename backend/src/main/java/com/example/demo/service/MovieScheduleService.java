package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.DTO.response.SingleMovieDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.response.MovieScheduleDTO;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Movie;
import com.example.demo.repository.BookingRepository;
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

    @Autowired
    BookingRepository bookingRepository;

    /**
     * Retrieves a list of movies that are currently showing as of the given date.
     *
     * @param currentDate the current date used to filter now showing movies
     * @return a list of {@link SingleMovieDTO} representing the now showing movies,
     *         each including their associated types
     */
    public List<SingleMovieDTO> getNowShowingMovie(LocalDate currentDate) {
        List<SingleMovieDTO> result = movieRepository.findNowShowingMovies(currentDate);

        for (SingleMovieDTO singleMovieDTO : result) {
            List<String> types = typeRepository.findTypeNamesByMovieId(singleMovieDTO.getId());
            singleMovieDTO.setTypes(types);
        }
        return result;
    }

    /**
     * Retrieves a list of movies that are coming soon after the given date.
     *
     * @param currentDate the current date used to filter upcoming movies
     * @return a list of {@link SingleMovieDTO} representing the upcoming movies,
     *         each including their associated types
     */
    public List<SingleMovieDTO> getCommingSoonMovie(LocalDate currentDate) {
        List<SingleMovieDTO> result = movieRepository.findUpcomingMovies(currentDate);
        for (SingleMovieDTO singleMovieDTO : result) {
            List<String> types = typeRepository.findTypeNamesByMovieId(singleMovieDTO.getId());
            singleMovieDTO.setTypes(types);
        }
        return result;
    }

    /**
     * Retrieves the full movie schedule for all movies on a specific date.
     *
     * @param date the date for which to retrieve the movie schedule
     * @return a list of {@link MovieScheduleDTO} containing the schedule details
     */
    public List<MovieScheduleDTO> getSchedule(LocalDate date) {
        List<MovieScheduleDTO> result = screeningService.getAllMovieScheduleByDate(date);
        return result;
    }

    /**
     * Retrieves detailed information for a specific movie by its ID.
     *
     * @param id the ID of the movie to retrieve
     * @return a {@link SingleMovieDTO} containing all movie details and types
     * @throws NotFoundException if no movie is found with the given ID
     */
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
                .director(movie.getDirector())
                .movieProductionCompany(movie.getMovieProductionCompany())
                .types(types)
                .build();
        return singleMovieDTO;
    }

    public long getTotalNowShowingMovie() {
        LocalDate now = LocalDate.now();
        return movieRepository.fetchTotalNowShowingMovies(now);
    }

    /**
     * Retrieves the top 5 most booked movies that are currently showing.
     *
     * @return a list of {@link SingleMovieDTO} representing the top 5 most booked
     *         movies,
     *         each including their associated types
     */
    public List<SingleMovieDTO> getTopBookedMovieByDate() {
        List<SingleMovieDTO> listTopMovie = bookingRepository.getTopBookedCurrentMovies(LocalDate.now());

        for (SingleMovieDTO singleMovieDTO : listTopMovie) {
            List<String> types = typeRepository.findTypeNamesByMovieId(singleMovieDTO.getId());
            singleMovieDTO.setTypes(types);
        }
        return listTopMovie.stream().limit(5).toList();
    }

}
