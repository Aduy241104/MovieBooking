package com.example.demo.DTO.response.dashboard;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopMovieByRevenueResponse {
    private String movieTitle;
    private String movieGenre;
    private double avgRating;
    private int ticketSold;
    private long revenue;
    private String posterUrl;
}
