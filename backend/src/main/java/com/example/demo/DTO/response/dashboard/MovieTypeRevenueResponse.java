package com.example.demo.DTO.response.dashboard;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
public class MovieTypeRevenueResponse {

    private List<MovieTypeRevenue> data;

    @Setter
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MovieTypeRevenue {
        private String movieType;
        private int quantity;
        private Long revenue;
    }
}
