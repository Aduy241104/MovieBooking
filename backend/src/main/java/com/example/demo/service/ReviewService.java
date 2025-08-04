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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnpaidBookingException;
import com.example.demo.exception.BadRequestException;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Service class that handles business logic for managing movie reviews.
 */
@Service
@AllArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final MovieRepository movieRepo;
    private final AccountRepository accountRepo;
    private final ReviewMapper reviewMapper;
    @Autowired
    private BookingRepository bookingRepo;


    /**
     * Adds a new review for a movie by a specific account.
     *
     * @param dto the DTO containing review details, including movieId and accountId
     * @return the created review as a ReviewResponseDTO
     * @throws BadRequestException if the user has already reviewed the movie
     * @throws UnpaidBookingException if the user hasn't purchased a paid booking for the movie
     * @throws NotFoundException if the movie or account is not found
     */
    public ReviewResponseDTO addReview(ReviewRequestDTO dto) {

        // Check if the user has already reviewed this movie
         if (reviewRepo.hasReviewed(dto.getAccountId(), dto.getMovieId())) {
            throw new BadRequestException("Bạn đã đánh giá phim này rồi!");
        }

         // Check if user has a paid booking for this movie
        boolean hasPaid = bookingRepo.countPaidBookings(dto.getAccountId(), dto.getMovieId()) > 0;
        if (!hasPaid) {
            throw new UnpaidBookingException("Bạn cần mua vé và thanh toán trước khi đánh giá phim này.");

        }

        // Validate and fetch the related movie and account
        Movie movie = movieRepo.findById(dto.getMovieId()).orElseThrow(
                () -> new NotFoundException("Movie not found"));
        Account account = accountRepo.findById(dto.getAccountId()).orElseThrow(
                () -> new NotFoundException("Account not found"));
        
        // Map DTO to entity and save        
        Review review = reviewMapper.toEntity(dto, movie, account);
        reviewRepo.save(review);
        return reviewMapper.toResponseDTO(review);
    }

    /**
     * Updates an existing review by its ID using the provided DTO.
     *
     * @param id the ID of the review to update
     * @param dto the DTO containing updated review details
     * @return the updated review as a ReviewResponseDTO
     * @throws NotFoundException if the review with the given ID is not found
     */
    public ReviewResponseDTO updateReview(Long id, ReviewRequestDTO dto) {
        Review review = reviewRepo.findById(id).orElseThrow(
                () -> new NotFoundException("Review not found"));
        
        // Update review fields from the DTO
        reviewMapper.updateReview(review, dto);
        reviewRepo.save(review);
        return reviewMapper.toResponseDTO(review);
    }

     /**
     * Permanently deletes a review by its ID.
     *
     * @param id the ID of the review to delete
     * @throws NotFoundException if the review with the given ID does not exist
     */ 
    public void deleteReview(Long id) {
        if (!reviewRepo.existsById(id)) {
            throw new NotFoundException("Review not found");
        }
        reviewRepo.deleteById(id);
    }

    /**
     * Retrieves the movie information and its reviews, wrapped in a single DTO.
     *
     * @param movieId the ID of the movie
     * @return a list containing a single MovieReviewWrapperDTO with movie and reviews
     * @throws NotFoundException if the movie with the given ID does not exist
     */
    public List<MovieReviewWrapperDTO> getWrappedReviewsByMovieId(Long movieId) {
        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Movie not found"));

        List<Review> reviews = reviewRepo.findByMovieId(movieId);

        List<ReviewResponseDTO> reviewDTOs = reviews.stream()
                .map(reviewMapper::toResponseDTO)
                .collect(Collectors.toList());

        MovieReviewWrapperDTO wrapper = new MovieReviewWrapperDTO(
                movie.getId(),
                movie.getNameVN(), /// You can use movie.getNameEn() for English name
                reviewDTOs);

        return List.of(wrapper); // Return as a singleton list
    }

    /**
     * Retrieves a paginated list of reviews for a specific movie.
     *
     * @param movieId the ID of the movie
     * @param page the page number to retrieve (0-based)
     * @param size the number of reviews per page
     * @return a paginated list of ReviewResponseDTO
     */
    public Page<ReviewResponseDTO> getPaginatedReviewsByMovieId(Long movieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reviewDate").descending());
        Page<Review> reviewPage = reviewRepo.findByMovieId(movieId, pageable);
        return reviewPage.map(reviewMapper::toResponseDTO);
    }

    
    // Get total number of approved reviews
    public long getTotalApprovedReviews() {
        return reviewRepo.countByApproved(true);
    }

    public Double findAverageRatingOfApproved() {
        Double average = reviewRepo.findAverageRatingOfApproved();
        return average != null ? average : 0.0;
    }

    // Lấy tất cả đánh giá của một phim
    public List<Review> getReviewsByMovieId(Long movieId) {
        return reviewRepo.findByMovieId(movieId);
    }

    // Đếm số đánh giá được duyệt của một phim
    public long getTotalApprovedReviewsByMovieId(Long movieId) {
        return reviewRepo.countByMovieIdAndApproved(movieId, true);
    }

    // Tính trung bình sao của một phim
    public Double getAverageRatingByMovieId(Long movieId) {
        Double average = reviewRepo.findAverageRatingByMovieId(movieId);
        return average != null ? average : 0.0;
    }

    // Xóa mềm một đánh giá
    public void softDeleteReview(Long reviewId) {
        reviewRepo.findById(reviewId).ifPresent(review -> {
            review.setIsDeleted(true);
            reviewRepo.save(review);
        });
    }

}
