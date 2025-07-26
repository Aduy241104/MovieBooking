////package com.example.demo.service;
////
////import com.example.demo.DTO.request.MovieRequest;
////import com.example.demo.DTO.response.MovieResponse;
////import com.example.demo.model.*;
////import com.example.demo.repository.AccountRepository;
////import com.example.demo.repository.MovieRepository;
////import com.example.demo.repository.MovieTypeRepository;
////import com.example.demo.repository.TypeRepository;
////import com.example.demo.utils.SecurityUtils;
////import lombok.RequiredArgsConstructor;
////import org.springframework.stereotype.Service;
////import org.springframework.web.multipart.MultipartFile;
////
////import jakarta.transaction.Transactional;
////import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
////
////import java.io.IOException;
////import java.nio.file.*;
////import java.util.*;
////import java.util.stream.Collectors;
////
////@Service
////@RequiredArgsConstructor
////public class MovieService {
////
////    private final MovieRepository movieRepository;
////    private final TypeRepository typeRepository;
////    private final MovieTypeRepository movieTypeRepository;
////    private final AccountRepository accountRepository;
////    private final ActivityLogService activityLogService;
////    private final NotificationService notificationService;
////
////    private final Path uploadRoot = Paths.get("uploads");
////
////    // ===== PUBLIC APIs =====
////
////    @Transactional
////    public MovieResponse createMovie(MovieRequest request) {
////    try {
////        validateMovieNameUniqueness(request.getNameVN(), request.getNameEN(), null);
////
////        Movie movie = new Movie();
////        applyRequestToMovie(movie, request);
////        handleFileAndTrailer(movie, request);
////
////        movieRepository.save(movie);
////        saveMovieTypes(movie, request.getTypeIds());
////
////        logActionAndNotifyAdmins(movie, "TẠO MỚI", "Tạo mới phim");
////
////        return convertToResponse(movie);
////
////    } catch (Exception e) {
////        System.err.println("Lỗi khi tạo phim: " + e.getMessage());
////        // Ném lại lỗi gốc thay vì tạo lỗi mới
////        throw e;
////    }
////}
////
////    @Transactional
////    public MovieResponse updateMovie(Long movieId, MovieRequest request) {
////    try {
////        Movie movie = movieRepository.findById(movieId)
////                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim"));
////
////        validateMovieNameUniqueness(request.getNameVN(), request.getNameEN(), movieId);
////        applyRequestToMovie(movie, request);
////        handleFileAndTrailer(movie, request);
////
////        movieRepository.save(movie);
////        updateMovieTypes(movieId, request.getTypeIds());
////
////        logActionAndNotifyAdmins(movie, "CẬP NHẬT", "Cập nhật thông tin phim");
////
////        return convertToResponse(movie);
////
////    } catch (Exception e) {
////        System.err.println("Lỗi khi cập nhật phim: " + e.getMessage());
////        // Ném lại lỗi gốc để frontend xử lý đúng
////        throw e;
////    }
////}
////
////    public List<MovieResponse> getAllMovies() {
////        try {
////            return movieRepository.findByIsDeletedFalse()
////                    .stream()
////                    .map(this::convertToResponse)
////                    .collect(Collectors.toList());
////        } catch (Exception e) {
////            System.err.println("Lỗi khi lấy danh sách phim: " + e.getMessage());
////            return Collections.emptyList();
////        }
////    }
////
////    public MovieResponse getMovieById(Long id) {
////        try {
////            Movie movie = movieRepository.findById(id)
////                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với id: " + id));
////            return convertToResponse(movie);
////        } catch (Exception e) {
////            System.err.println("Lỗi khi lấy thông tin phim: " + e.getMessage());
////            throw new RuntimeException("Không thể lấy thông tin phim.");
////        }
////    }
////
////    @Transactional
////    public void deleteMovie(Long id) {
////        try {
////            Movie movie = movieRepository.findById(id)
////                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phim để xóa"));
////
////            movie.setIsDeleted(true);
////            movieRepository.save(movie);
////
////            logActionAndNotifyAdmins(movie, "XOÁ", "Xoá phim");
////        } catch (Exception e) {
////            System.err.println("Lỗi khi xoá phim: " + e.getMessage());
////            throw new RuntimeException("Không thể xoá phim.");
////        }
////    }
////
////    // ===== PRIVATE HELPERS =====
////
////    private void validateMovieNameUniqueness(String nameVN, String nameEN, Long excludeId) {
////        movieRepository.findByNameVNAndIsDeletedFalse(nameVN)
////                .filter(m -> excludeId == null || !m.getId().equals(excludeId))
////                .ifPresent(m -> {
////                    throw new RuntimeException("Tên phim (VN) đã tồn tại");
////                });
////
////        movieRepository.findByNameENAndIsDeletedFalse(nameEN)
////                .filter(m -> excludeId == null || !m.getId().equals(excludeId))
////                .ifPresent(m -> {
////                    throw new RuntimeException("Tên phim (EN) đã tồn tại");
////                });
////    }
////
////    private void applyRequestToMovie(Movie movie, MovieRequest request) {
////        movie.setNameVN(request.getNameVN());
////        movie.setNameEN(request.getNameEN());
////        movie.setContent(request.getContent());
////        movie.setDuration(request.getDuration());
////        movie.setFromDate(request.getFromDate());
////        movie.setToDate(request.getToDate());
////        movie.setDirector(request.getDirector());
////        movie.setActor(request.getActor());
////        movie.setMovieProductionCompany(request.getMovieProductionCompany());
////        movie.setAgeLimit(request.getAgeLimit());
////    }
////
////    private void handleFileAndTrailer(Movie movie, MovieRequest request) {
////        if (request.getSmallImage() != null) {
////            movie.setSmallImage(saveFile(request.getSmallImage(), "images"));
////        }
////        if (request.getLargeImage() != null) {
////            movie.setLargeImage(saveFile(request.getLargeImage(), "images"));
////        }
////        if (request.getTrailerLink() != null && !request.getTrailerLink().isBlank()) {
////            movie.setTrailer(request.getTrailerLink());
////        }
////    }
////
////    private void saveMovieTypes(Movie movie, List<Long> typeIds) {
////        if (typeIds == null) return;
////        List<Type> types = typeRepository.findAllByIdIn(typeIds);
////        types.forEach(type -> movieTypeRepository.save(new MovieType(null, movie, type)));
////    }
////
////    private void updateMovieTypes(Long movieId, List<Long> typeIds) {
////        movieTypeRepository.deleteByMovieId(movieId);
////        movieRepository.flush(); // ensure delete happens before insert
////
////        if (typeIds != null) {
////            Movie movie = movieRepository.findById(movieId).orElseThrow();
////            saveMovieTypes(movie, typeIds);
////        }
////    }
////
////    private MovieResponse convertToResponse(Movie movie) {
////        List<MovieType> movieTypes = movieTypeRepository.findAll()
////                .stream()
////                .filter(mt -> mt.getMovie().getId().equals(movie.getId()))
////                .toList();
////
////        List<String> typeNames = movieTypes.stream().map(mt -> mt.getType().getName()).toList();
////        List<Long> typeIds = movieTypes.stream()
////                .map(mt -> mt.getType().getId().longValue())
////                .toList();
////
////        return new MovieResponse(
////                movie.getId(), movie.getNameVN(), movie.getNameEN(),
////                movie.getDuration(), movie.getContent(),
////                movie.getFromDate(), movie.getToDate(),
////                movie.getSmallImage(), movie.getLargeImage(), movie.getTrailer(),
////                movie.getDirector(), movie.getActor(),
////                movie.getMovieProductionCompany(), movie.getAgeLimit(),
////                typeNames, typeIds
////        );
////    }
////
////    private String saveFile(MultipartFile file, String subfolder) {
////        if (file == null || file.isEmpty()) return null;
////        try {
////            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
////            Path dir = uploadRoot.resolve(subfolder);
////            Files.createDirectories(dir);
////            Path filepath = dir.resolve(filename);
////            Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);
////            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
////            return baseUrl + "/images/" + filename;
////        } catch (IOException e) {
////            throw new RuntimeException("Lỗi khi lưu file: " + e.getMessage());
////        }
////    }
////
////    private void logActionAndNotifyAdmins(Movie movie, String action, String description) {
////        try {
////            String userId = SecurityUtils.getCurrentUsername();
////            if (userId == null || userId.isBlank()) return;
////
////            Account user = accountRepository.findById(Long.valueOf(userId))
////                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
////
////            activityLogService.log(user.getEmail(), action, "PHIM", movie.getNameVN(), description);
////
////            List<Account> admins = accountRepository.findByRole_RoleName("ADMIN");
////            for (Account admin : admins) {
////                if (!admin.getAccountId().equals(user.getAccountId())) {
////                    notificationService.notify(
////                            admin,
////                            description,
////                            user.getFullName() + " đã " + description.toLowerCase() + ": " + movie.getNameVN(),
////                            "SYSTEM"
////                    );
////                }
////            }
////        } catch (Exception e) {
////            System.err.println("Lỗi khi ghi log/notification: " + e.getMessage());
////        }
////    }
////}
//package com.example.demo.service;
//
//import com.example.demo.DTO.request.MovieRequest;
//import com.example.demo.DTO.response.MovieResponse;
//import com.example.demo.exception.DuplicateNameException;
//import com.example.demo.exception.FileStorageException;
//import com.example.demo.exception.NotFoundException;
//import com.example.demo.model.*;
//import com.example.demo.repository.AccountRepository;
//import com.example.demo.repository.MovieRepository;
//import com.example.demo.repository.MovieTypeRepository;
//import com.example.demo.repository.TypeRepository;
//import com.example.demo.utils.SecurityUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import jakarta.transaction.Transactional;
//import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
//
//import java.io.IOException;
//import java.nio.file.*;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class MovieService {
//
//    private final MovieRepository movieRepository;
//    private final TypeRepository typeRepository;
//    private final MovieTypeRepository movieTypeRepository;
//    private final AccountRepository accountRepository;
//    private final ActivityLogService activityLogService;
//    private final NotificationService notificationService;
//
//    private final Path uploadRoot = Paths.get("uploads");
//
//    @Transactional
//    public MovieResponse createMovie(MovieRequest request) {
//        validateMovieNameUniqueness(request.getNameVN(), request.getNameEN(), null);
//
//        Movie movie = new Movie();
//        applyRequestToMovie(movie, request);
//        handleFileAndTrailer(movie, request);
//
//        movieRepository.save(movie);
//        saveMovieTypes(movie, request.getTypeIds());
//
//        logActionAndNotifyAdmins(movie, "TẠO MỚI", "Tạo mới phim");
//
//        return convertToResponse(movie);
//    }
//
//    @Transactional
//    public MovieResponse updateMovie(Long movieId, MovieRequest request) {
//        Movie movie = movieRepository.findById(movieId)
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy phim với ID: " + movieId));
//
//        validateMovieNameUniqueness(request.getNameVN(), request.getNameEN(), movieId);
//        applyRequestToMovie(movie, request);
//        handleFileAndTrailer(movie, request);
//
//        movieRepository.save(movie);
//        updateMovieTypes(movieId, request.getTypeIds());
//
//        logActionAndNotifyAdmins(movie, "CẬP NHẬT", "Cập nhật thông tin phim");
//
//        return convertToResponse(movie);
//    }
//
//    public List<MovieResponse> getAllMovies() {
//        return movieRepository.findByIsDeletedFalse()
//                .stream()
//                .map(this::convertToResponse)
//                .collect(Collectors.toList());
//    }
//
//    public MovieResponse getMovieById(Long id) {
//        Movie movie = movieRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy phim với ID: " + id));
//        return convertToResponse(movie);
//    }
//
//    @Transactional
//    public void deleteMovie(Long id) {
//        Movie movie = movieRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy phim để xóa"));
//
//        movie.setIsDeleted(true);
//        movieRepository.save(movie);
//
//        logActionAndNotifyAdmins(movie, "XOÁ", "Xoá phim");
//    }
//
//    private void validateMovieNameUniqueness(String nameVN, String nameEN, Long excludeId) {
//        movieRepository.findByNameVNAndIsDeletedFalse(nameVN)
//                .filter(m -> excludeId == null || !m.getId().equals(excludeId))
//                .ifPresent(m -> {
//                    throw new DuplicateNameException("Tên phim (VN) đã tồn tại");
//                });
//
//        movieRepository.findByNameENAndIsDeletedFalse(nameEN)
//                .filter(m -> excludeId == null || !m.getId().equals(excludeId))
//                .ifPresent(m -> {
//                    throw new DuplicateNameException("Tên phim (EN) đã tồn tại");
//                });
//    }
//
//    private void applyRequestToMovie(Movie movie, MovieRequest request) {
//        movie.setNameVN(request.getNameVN());
//        movie.setNameEN(request.getNameEN());
//        movie.setContent(request.getContent());
//        movie.setDuration(request.getDuration());
//        movie.setFromDate(request.getFromDate());
//        movie.setToDate(request.getToDate());
//        movie.setDirector(request.getDirector());
//        movie.setActor(request.getActor());
//        movie.setMovieProductionCompany(request.getMovieProductionCompany());
//        movie.setAgeLimit(request.getAgeLimit());
//    }
//
//    private void handleFileAndTrailer(Movie movie, MovieRequest request) {
//        if (request.getSmallImage() != null) {
//            movie.setSmallImage(saveFile(request.getSmallImage(), "images"));
//        }
//        if (request.getLargeImage() != null) {
//            movie.setLargeImage(saveFile(request.getLargeImage(), "images"));
//        }
//        if (request.getTrailerLink() != null && !request.getTrailerLink().isBlank()) {
//            movie.setTrailer(request.getTrailerLink());
//        }
//    }
//
//    private void saveMovieTypes(Movie movie, List<Long> typeIds) {
//        if (typeIds == null) return;
//        List<Type> types = typeRepository.findAllByIdIn(typeIds);
//        types.forEach(type -> movieTypeRepository.save(new MovieType(null, movie, type)));
//    }
//
//    private void updateMovieTypes(Long movieId, List<Long> typeIds) {
//        movieTypeRepository.deleteByMovieId(movieId);
//        movieRepository.flush(); // ensure delete happens before insert
//
//        if (typeIds != null) {
//            Movie movie = movieRepository.findById(movieId)
//                    .orElseThrow(() -> new NotFoundException("Không tìm thấy phim để cập nhật thể loại"));
//            saveMovieTypes(movie, typeIds);
//        }
//    }
//
//    private MovieResponse convertToResponse(Movie movie) {
//        List<MovieType> movieTypes = movieTypeRepository.findAll()
//                .stream()
//                .filter(mt -> mt.getMovie().getId().equals(movie.getId()))
//                .toList();
//
//        List<String> typeNames = movieTypes.stream().map(mt -> mt.getType().getName()).toList();
//        List<Long> typeIds = movieTypes.stream()
//                .map(mt -> mt.getType().getId().longValue())
//                .toList();
//
//        return new MovieResponse(
//                movie.getId(), movie.getNameVN(), movie.getNameEN(),
//                movie.getDuration(), movie.getContent(),
//                movie.getFromDate(), movie.getToDate(),
//                movie.getSmallImage(), movie.getLargeImage(), movie.getTrailer(),
//                movie.getDirector(), movie.getActor(),
//                movie.getMovieProductionCompany(), movie.getAgeLimit(),
//                typeNames, typeIds
//        );
//    }
//
//    private String saveFile(MultipartFile file, String subfolder) {
//        if (file == null || file.isEmpty()) return null;
//        try {
//            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            Path dir = uploadRoot.resolve(subfolder);
//            Files.createDirectories(dir);
//            Path filepath = dir.resolve(filename);
//            Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);
//            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
//            return baseUrl + "/images/" + filename;
//        } catch (IOException e) {
//            throw new FileStorageException("Lỗi khi lưu file: " + e.getMessage(), e);
//        }
//    }
//
//    private void logActionAndNotifyAdmins(Movie movie, String action, String description) {
//        try {
//            String userId = SecurityUtils.getCurrentUsername();
//            if (userId == null || userId.isBlank()) return;
//
//            Account user = accountRepository.findById(Long.valueOf(userId))
//                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
//
//            activityLogService.log(user.getEmail(), action, "PHIM", movie.getNameVN(), description);
//
//            List<Account> admins = accountRepository.findByRole_RoleName("ADMIN");
//            for (Account admin : admins) {
//                if (!admin.getAccountId().equals(user.getAccountId())) {
//                    notificationService.notify(
//                            admin,
//                            description,
//                            user.getFullName() + " đã " + description.toLowerCase() + ": " + movie.getNameVN(),
//                            "SYSTEM"
//                    );
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Lỗi khi ghi log/notification: " + e.getMessage());
//        }
//    }
//}
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

        movie.setSmallImage(saveFile(request.getSmallImage(), "images"));
        movie.setLargeImage(saveFile(request.getLargeImage(), "images"));

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

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findByIsDeletedFalse()
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phim với id: " + id));
        return mapEntityToResponse(movie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phim để xoá"));
        movie.setIsDeleted(true);
        movieRepository.save(movie);
        setLogAndNotification(movie, "XOÁ", "Xoá phim", "Xoá phim", " đã xoá phim: ");
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
                .collect(Collectors.toList());

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
