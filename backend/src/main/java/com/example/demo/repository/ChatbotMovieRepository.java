package com.example.demo.repository;

import com.example.demo.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChatbotMovieRepository extends JpaRepository<Movie, Long> {

    // Phim đang chiếu (trong khoảng thời gian hiện tại)
    @Query("SELECT m FROM Movie m WHERE m.fromDate <= :currentDate AND m.toDate >= :currentDate AND m.isDeleted = false ORDER BY m.fromDate DESC")
    List<Movie> findNowShowingMovies(@Param("currentDate") LocalDate currentDate);

    // Phim sắp chiếu (sau ngày hiện tại)
    @Query("SELECT m FROM Movie m WHERE m.fromDate > :currentDate AND m.isDeleted = false ORDER BY m.fromDate ASC")
    List<Movie> findComingSoonMovies(@Param("currentDate") LocalDate currentDate);

    // Phim mới nhất (theo ngày tạo hoặc ngày chiếu)
    @Query("SELECT m FROM Movie m WHERE m.isDeleted = false ORDER BY m.fromDate DESC")
    List<Movie> findNewestMovies();

    // Top phim hot (theo số lượng đánh giá hoặc rating)
    @Query("SELECT m FROM Movie m LEFT JOIN Review r ON m.id = r.movie.id " +
            "WHERE m.isDeleted = false " +
            "GROUP BY m.id " +
            "ORDER BY COUNT(r.id) DESC, AVG(r.rating) DESC")
    List<Movie> findTopMovies();

    // Tìm phim theo tên (cả tiếng Việt và tiếng Anh)
    @Query("SELECT m FROM Movie m WHERE (LOWER(m.nameVN) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.nameEN) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND m.isDeleted = false")
    List<Movie> findByNameContaining(@Param("keyword") String keyword);

    // Phim có phụ đề tiếng Việt
//    @Query("SELECT m FROM Movie m WHERE LOWER(m.language) LIKE '%subtitle%' OR LOWER(m.language) LIKE '%phụ đề%' AND m.isDeleted = false")
//    List<Movie> findMoviesWithSubtitle();

//    // Phim lồng tiếng
//    @Query("SELECT m FROM Movie m WHERE LOWER(m.language) LIKE '%dubbed%' OR LOWER(m.language) LIKE '%lồng tiếng%' AND m.isDeleted = false")
//    List<Movie> findDubbedMovies();

    // Phim 3D
    @Query("SELECT m FROM Movie m WHERE LOWER(m.nameVN) LIKE '%3d%' OR LOWER(m.nameEN) LIKE '%3d%' AND m.isDeleted = false")
    List<Movie> find3DMovies();
}
