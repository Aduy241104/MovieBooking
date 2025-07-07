package com.example.demo.service;

import com.example.demo.DTO.request.ReviewRequestDTO;
import com.example.demo.DTO.response.MovieReviewWrapperDTO;
import com.example.demo.DTO.response.ReviewResponseDTO;
import com.example.demo.mapper.ReviewMapper;
import com.example.demo.model.Account;
import com.example.demo.model.Movie;
import com.example.demo.model.Review;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.ReviewRepository;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final MovieRepository movieRepo;
    private final ReviewRepository reviewRepository;
    private final AccountRepository accountRepo;
    private final ReviewMapper reviewMapper;
    @Autowired
    private BookingRepository bookingRepo;

    // 1. Xem danh sách review theo movieId (dùng Long)
    public List<ReviewResponseDTO> getReviewsByMovieId(Long movieId) {
        List<Review> reviews = reviewRepo.findByMovieId(movieId); // Tên method trong repository
        return reviews.stream()
                .map(reviewMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // 2. Thêm review mới
    public ReviewResponseDTO addReview(ReviewRequestDTO dto) {

        if (reviewRepo.hasReviewed(dto.getAccountId(), dto.getMovieId())) {
            throw new RuntimeException("Bạn đã đánh giá phim này rồi!");
        }

        // Kiểm tra đã đặt và thanh toán chưa
        boolean hasPaid = bookingRepo.countPaidBookings(dto.getAccountId(), dto.getMovieId()) > 0;
        System.out.println("result");
        System.out.println(bookingRepo.countPaidBookings(dto.getAccountId(), dto.getMovieId()));

        if (!hasPaid) {
            throw new RuntimeException("Bạn cần mua vé và thanh toán trước khi đánh giá phim này.");

        }

        Movie movie = movieRepo.findById(dto.getMovieId()).orElseThrow(
                () -> new RuntimeException("Movie not found"));
        Account account = accountRepo.findById(dto.getAccountId()).orElseThrow(
                () -> new RuntimeException("Account not found"));
        Review review = reviewMapper.toEntity(dto, movie, account);
        reviewRepo.save(review);
        return reviewMapper.toResponseDTO(review);
    }

    // 3. Sửa review theo id
    public ReviewResponseDTO updateReview(Integer id, ReviewRequestDTO dto) {
        Review review = reviewRepo.findById(id).orElseThrow(
                () -> new RuntimeException("Review not found"));
        reviewMapper.updateReview(review, dto);
        reviewRepo.save(review);
        return reviewMapper.toResponseDTO(review);
    }

    // 4. Xoá review theo id
    public void deleteReview(Integer id) {
        if (!reviewRepo.existsById(id)) {
            throw new RuntimeException("Review not found");
        }
        reviewRepo.deleteById(id);
    }

    public List<MovieReviewWrapperDTO> getWrappedReviewsByMovieId(Long movieId) {
        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        List<Review> reviews = reviewRepo.findByMovieId(movieId);

        List<ReviewResponseDTO> reviewDTOs = reviews.stream()
                .map(reviewMapper::toResponseDTO)
                .collect(Collectors.toList());

        MovieReviewWrapperDTO wrapper = new MovieReviewWrapperDTO(
                movie.getId(),
                movie.getNameVN(), // hoặc getMovieNameEn nếu muốn tên tiếng Anh
                reviewDTOs);

        return List.of(wrapper); // Trả về 1 phần tử dưới dạng list
    }

    public Page<ReviewResponseDTO> getPaginatedReviewsByMovieId(Long movieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepo.findByMovieId(movieId, pageable);

        System.out.println("MovieId: " + movieId);
        System.out.println("Tổng số review tìm được: " + reviewPage.getTotalElements());
        System.out.println("Nội dung trang hiện tại: " + reviewPage.getContent());

        return reviewPage.map(reviewMapper::toResponseDTO);
    }

    public long getTotalApprovedReviews() {
        return reviewRepository.countByApproved(true);
    }

    public Double findAverageRatingOfApproved() {
        return reviewRepository.findAverageRatingOfApproved();
    }
}
