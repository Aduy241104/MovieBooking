package com.example.demo.service;

import com.example.demo.DTO.request.ReviewRequestDTO;
import com.example.demo.DTO.response.MovieReviewWrapperDTO;
import com.example.demo.DTO.response.ReviewResponseDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnpaidBookingException;
import com.example.demo.mapper.ReviewMapper;
import com.example.demo.model.Account;
import com.example.demo.model.Movie;
import com.example.demo.model.Review;
import com.example.demo.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private MovieRepository movieRepo;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddReview_Success() {
        // Arrange
        Long movieId = 1L;
        Long accountId = 2L;

        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setMovieId(movieId);
        dto.setAccountId(accountId);
        dto.setRating(4);
        dto.setComment("Phim rất hay");

        Movie movie = new Movie();
        movie.setId(movieId);
        Account account = new Account();
        account.setAccountId(accountId); // không bị lỗi nữa

        Review review = new Review();
        ReviewResponseDTO responseDTO = new ReviewResponseDTO();

        when(reviewRepo.hasReviewed(accountId, movieId)).thenReturn(false);
        when(bookingRepo.countPaidBookings(accountId, movieId)).thenReturn(1L);
        when(movieRepo.findById(movieId)).thenReturn(java.util.Optional.of(movie));
        when(accountRepo.findById(accountId)).thenReturn(java.util.Optional.of(account));
        when(reviewMapper.toEntity(dto, movie, account)).thenReturn(review);
        when(reviewMapper.toResponseDTO(review)).thenReturn(responseDTO);

        // Act
        ReviewResponseDTO result = reviewService.addReview(dto);

        // Assert
        assertNotNull(result);
        verify(reviewRepo).save(review);
    }

    @Test
    void testAddReview_AlreadyReviewed() {
        // Arrange
        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setMovieId(1L);
        dto.setAccountId(2L);
        dto.setRating(5);
        dto.setComment("test");

        when(reviewRepo.hasReviewed(2L, 1L)).thenReturn(true);

        // Act + Assert
        assertThrows(BadRequestException.class, () -> reviewService.addReview(dto));
    }

    @Test
    void testAddReview_NotPaid() {
        // Arrange
        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setMovieId(1L);
        dto.setAccountId(2L);
        dto.setRating(5);
        dto.setComment("test");

        when(reviewRepo.hasReviewed(2L, 1L)).thenReturn(false);
        when(bookingRepo.countPaidBookings(2L, 1L)).thenReturn(0L);

        // Act + Assert
        assertThrows(UnpaidBookingException.class, () -> reviewService.addReview(dto));
    }

    @Test
    void testAddReview_MovieNotFound() {
        // Arrange
        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setMovieId(1L);
        dto.setAccountId(2L);
        dto.setRating(5);
        dto.setComment("test");

        when(reviewRepo.hasReviewed(2L, 1L)).thenReturn(false);
        when(bookingRepo.countPaidBookings(2L, 1L)).thenReturn(1L);
        when(movieRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> reviewService.addReview(dto));
    }

    @Test
    void testAddReview_AccountNotFound() {
        // Arrange
        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setMovieId(1L);
        dto.setAccountId(2L);
        dto.setRating(5);
        dto.setComment("test");

        when(reviewRepo.hasReviewed(2L, 1L)).thenReturn(false);
        when(bookingRepo.countPaidBookings(2L, 1L)).thenReturn(1L);
        when(movieRepo.findById(1L)).thenReturn(java.util.Optional.of(new Movie()));
        when(accountRepo.findById(2L)).thenReturn(java.util.Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> reviewService.addReview(dto));
    }

    @Test
    void testUpdateReview_Success() {
        // Given
        Long reviewId = 1L;
        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setRating(4);
        dto.setComment("Very good");
        dto.setSpoilerAlert(false);
        dto.setApproved(true);

        Review existingReview = new Review();
        existingReview.setId(reviewId);

        Review updatedReview = new Review();
        updatedReview.setId(reviewId);
        updatedReview.setRating(4);
        updatedReview.setComment("Very good");
        updatedReview.setSpoilerAlert(false);
        updatedReview.setApproved(true);
        updatedReview.setReviewDate(LocalDateTime.now());

        ReviewResponseDTO expectedResponse = ReviewResponseDTO.builder()
                .id(reviewId)
                .rating(4)
                .comment("Very good")
                .spoilerAlert(false)
                .approved(true)
                .reviewDate(updatedReview.getReviewDate())
                .build();

        when(reviewRepo.findById(reviewId)).thenReturn(Optional.of(existingReview));
        doAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setRating(dto.getRating());
            r.setComment(dto.getComment());
            r.setSpoilerAlert(dto.getSpoilerAlert());
            r.setApproved(dto.getApproved());
            return null;
        }).when(reviewMapper).updateReview(existingReview, dto);

        when(reviewRepo.save(any(Review.class))).thenReturn(updatedReview);
        when(reviewMapper.toResponseDTO(any(Review.class))).thenReturn(expectedResponse);

        // When
        ReviewResponseDTO actual = reviewService.updateReview(reviewId, dto);

        // Then
        assertNotNull(actual);
        assertEquals(expectedResponse.getId(), actual.getId());
        assertEquals(expectedResponse.getRating(), actual.getRating());
        assertEquals(expectedResponse.getComment(), actual.getComment());
    }

    @Test
    void testUpdateReview_ReviewNotFound() {
        Long reviewId = 100L;
        ReviewRequestDTO dto = new ReviewRequestDTO();
        when(reviewRepo.findById(reviewId)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> reviewService.updateReview(reviewId, dto));

        assertEquals("Review not found", thrown.getMessage());
    }

    @Test
    void testDeleteReview_Success() {
        Long reviewId = 1L;

        when(reviewRepo.existsById(reviewId)).thenReturn(true);

        reviewService.deleteReview(reviewId);

        verify(reviewRepo, times(1)).deleteById(reviewId);
    }


      @Test
    void testGetWrappedReviewsByMovieId_Success() {
        Long movieId = 1L;

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setNameVN("Tên phim");

        Review review = new Review();
        review.setId(10L);

        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(10L);

        when(movieRepo.findById(movieId)).thenReturn(Optional.of(movie));
        when(reviewRepo.findByMovieId(movieId)).thenReturn(List.of(review));
        when(reviewMapper.toResponseDTO(review)).thenReturn(dto);

        List<MovieReviewWrapperDTO> result = reviewService.getWrappedReviewsByMovieId(movieId);

        assertEquals(1, result.size());
        MovieReviewWrapperDTO wrapper = result.get(0);
        assertEquals(movieId, wrapper.getMovieid());
        assertEquals("Tên phim", wrapper.getMovieName());
        assertEquals(1, wrapper.getReviews().size());
        assertEquals(10L, wrapper.getReviews().get(0).getId());
    }

    @Test
    void testGetWrappedReviewsByMovieId_MovieNotFound() {
        Long movieId = 999L;
        when(movieRepo.findById(movieId)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> reviewService.getWrappedReviewsByMovieId(movieId)
        );

        assertEquals("Movie not found", thrown.getMessage());
    }

     @Test
    void testGetPaginatedReviewsByMovieId() {
        // Given
        Long movieId = 1L;
        int page = 0;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size, Sort.by("reviewDate").descending());

        Review review1 = new Review();
        Review review2 = new Review();

        Page<Review> reviewPage = new PageImpl<>(List.of(review1, review2));

        when(reviewRepo.findByMovieId(eq(movieId), any(Pageable.class)))
                .thenReturn(reviewPage);

        ReviewResponseDTO dto1 = new ReviewResponseDTO();
        ReviewResponseDTO dto2 = new ReviewResponseDTO();

        when(reviewMapper.toResponseDTO(review1)).thenReturn(dto1);
        when(reviewMapper.toResponseDTO(review2)).thenReturn(dto2);

        // When
        Page<ReviewResponseDTO> result = reviewService.getPaginatedReviewsByMovieId(movieId, page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(reviewRepo).findByMovieId(eq(movieId), any(Pageable.class));
        verify(reviewMapper, times(1)).toResponseDTO(review1);
        verify(reviewMapper, times(1)).toResponseDTO(review2);
    }
}
