package com.example.demo.mapper;

import com.example.demo.DTO.request.ReviewRequestDTO;
import com.example.demo.DTO.response.ReviewResponseDTO;
import com.example.demo.model.Account;
import com.example.demo.model.Movie;
import com.example.demo.model.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public ReviewResponseDTO toResponseDTO(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getAccount().getAccountId(),
                review.getAccount().getAvatar(),
                review.getAccount().getFullName(),
                review.getRating(),
                review.getComment(),
                review.getReviewDate(),
                review.getApproved(),
                review.getSpoilerAlert());
    }

    public Review toEntity(ReviewRequestDTO dto, Movie movie, Account account) {
        return Review.builder()
                .movie(movie)
                .account(account)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .reviewDate(dto.getReviewDate())
                .approved(dto.getApproved())
                .spoilerAlert(dto.getSpoilerAlert())
                .build();
    }

    public void updateReview(Review review, ReviewRequestDTO dto) {
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setReviewDate(dto.getReviewDate());
        review.setApproved(dto.getApproved());
        review.setSpoilerAlert(dto.getSpoilerAlert());
    }
}
