package com.example.demo.service;

import com.example.demo.DTO.request.MovieRequest;
import com.example.demo.DTO.response.MovieResponse;
import com.example.demo.exception.DuplicateNameException;
import com.example.demo.exception.FileStorageException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.AppException;
import com.example.demo.model.*;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.MovieTypeRepository;
import com.example.demo.repository.TypeRepository;
import com.example.demo.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    private final AccountRepository accountRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    private final Path uploadRoot = Paths.get("uploads");

    /**
     * Saves the given file to the specified subfolder under the "uploads" directory.
     * Generates a random filename and returns the public access URL.
     *
     * @param file      The multipart file to save.
     * @param subfolder The subfolder to store the file in (e.g., "images").
     * @return The URL to access the uploaded file.
     */
    private String saveFile(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) return null;

        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path dir = uploadRoot.resolve(subfolder);
            Files.createDirectories(dir);
            Path filepath = dir.resolve(filename);
            Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return baseUrl + "/images/" + filename;
        } catch (IOException ex) {
            throw new FileStorageException("Không thể lưu file: " + file.getOriginalFilename(), ex);
        }
    }

    /**
     * Creates a new movie with data from the MovieRequest.
     * - Validates for duplicate names.
     * - Saves movie images and trailer link.
     * - Saves movie and its associated types.
     * - Logs the activity and sends notifications to admins.
     *
     * @param request MovieRequest containing movie details.
     * @return MovieResponse with movie data after saving.
     */
    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        movieRepository.findByNameVNAndIsDeletedFalse(request.getNameVN())
                .ifPresent(m -> {
                    throw new DuplicateNameException("Tên phim (VN) đã tồn tại");
                });

        movieRepository.findByNameENAndIsDeletedFalse(request.getNameEN())
                .ifPresent(m -> {
                    throw new DuplicateNameException("Tên phim (EN) đã tồn tại");
                });

        Movie movie = new Movie();
        mapRequestToEntity(movie, request);

        movie.setSmallImage(saveFile(request.getSmallImage(), "images"));
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

        setLogAndNotification(movie, "TẠO MỚI", "Tạo mới phim", "Tạo mới phim", " đã tạo mới phim: ");
        return mapEntityToResponse(movie);
    }

    /**
     * Updates an existing movie by ID with new data from the MovieRequest.
     * - Validates for duplicate names (excluding current movie).
     * - Updates images only if new ones are uploaded.
     * - Updates associated types by clearing and recreating associations.
     * - Logs the update and notifies admins.
     *
     * @param movieId ID of the movie to update.
     * @param request New movie data.
     * @return MovieResponse with updated movie data.
     */
    @Transactional
    public MovieResponse updateMovie(Long movieId, MovieRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phim với ID: " + movieId));

        movieRepository.findByNameVNAndIsDeletedFalse(request.getNameVN())
                .filter(m -> !m.getId().equals(movieId))
                .ifPresent(m -> {
                    throw new DuplicateNameException("Tên phim (VN) đã tồn tại");
                });

        movieRepository.findByNameENAndIsDeletedFalse(request.getNameEN())
                .filter(m -> !m.getId().equals(movieId))
                .ifPresent(m -> {
                    throw new DuplicateNameException("Tên phim (EN) đã tồn tại");
                });

        mapRequestToEntity(movie, request);

        if (request.getSmallImage() != null && !request.getSmallImage().isEmpty()) {
            movie.setSmallImage(saveFile(request.getSmallImage(), "images"));
        }
        if (request.getLargeImage() != null && !request.getLargeImage().isEmpty()) {
            movie.setLargeImage(saveFile(request.getLargeImage(), "images"));
        }

        if (request.getTrailerLink() != null && !request.getTrailerLink().isBlank())
            movie.setTrailer(request.getTrailerLink());

        movieRepository.save(movie);
        movieTypeRepository.deleteByMovieId(movieId);
        movieRepository.flush();

        if (request.getTypeIds() != null) {
            List<Type> types = typeRepository.findAllByIdIn(request.getTypeIds());
            for (Type type : types) {
                movieTypeRepository.save(new MovieType(null, movie, type));
            }
        }

        setLogAndNotification(movie, "CẬP NHẬT", "Cập nhật thông tin phim", "Cập nhật thông tin phim", " đã cập nhật phim: ");
        return mapEntityToResponse(movie);
    }

    /**
     * Retrieves all non-deleted movies from the database.
     *
     * @return List of MovieResponse representing all movies.
     */
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findByIsDeletedFalse()
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a movie by its ID.
     *
     * @param id Movie ID.
     * @return MovieResponse containing movie data.
     * @throws NotFoundException if movie does not exist.
     */
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phim với id: " + id));
        return mapEntityToResponse(movie);
    }

    /**
     * Performs a soft delete on a movie by setting its isDeleted flag to true.
     * Logs the deletion and notifies admins.
     *
     * @param id ID of the movie to delete.
     * @throws NotFoundException if movie is not found.
     */
    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phim để xoá"));
        movie.setIsDeleted(true);
        movieRepository.save(movie);
        setLogAndNotification(movie, "XOÁ", "Xoá phim", "Xoá phim", " đã xoá phim: ");
    }

    /**
     * Maps fields from MovieRequest to a Movie entity object.
     *
     * @param movie   The Movie entity to populate.
     * @param request The MovieRequest containing new data.
     */
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

    /**
     * Converts a Movie entity into a MovieResponse.
     * Also loads type names and type IDs associated with the movie.
     *
     * @param movie The Movie entity.
     * @return MovieResponse for the movie.
     */
    private MovieResponse mapEntityToResponse(Movie movie) {
        List<MovieType> movieTypes = movieTypeRepository.findByMovie_Id(movie.getId());


        List<String> typeNames = movieTypes.stream()
                .map(mt -> mt.getType().getName())
                .collect(Collectors.toList());

        List<Long> typeIds = movieTypes.stream()
                .map(mt -> mt.getType().getId())
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

    /**
     * Logs the specified action (create, update, delete) to the ActivityLog.
     * Sends notifications to all admins except the acting user.
     *
     * @param movie     The movie involved in the action.
     * @param action    Action keyword (e.g., "CREATE", "UPDATE", "DELETE").
     * @param description Activity description for the log.
     * @param title     Title of the notification.
     * @param content   Notification content with movie name included.
     */
    private void setLogAndNotification(Movie movie, String action, String description,
                                       String title, String content) {
        String loginUserId = SecurityUtils.getCurrentUsername();

        if(loginUserId == null || loginUserId.isEmpty()) {
            throw new AppException("User not logged in");
        }
        Account user = accountRepository.findById(Long.valueOf(loginUserId))
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        // Log activity
        activityLogService.log(
                user.getEmail(),
                action,
                "PHIM",
                movie.getNameVN(),
                description
        );

        // Notify admins
        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        if(adminAccounts.isEmpty()) {
            throw new AppException("No admin accounts found");
        }
        for (Account admin : adminAccounts) {
            if (!admin.getAccountId().equals(user.getAccountId())) {
                notificationService.notify(
                        admin,
                        title,
                        user.getFullName() + content + movie.getNameVN(),
                        "SYSTEM"
                );
            }
        }
    }
}
