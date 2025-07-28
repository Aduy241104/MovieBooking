package com.example.demo.DTO.response.dashboard;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopMovieByRevenueResponse {
    private String movieTitle;
    private String movieGenre;
    private BigDecimal avgRating;
    private Long ticketSold;
    private BigDecimal revenue;
    private String posterUrl;
}
