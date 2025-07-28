package com.example.demo.DTO.response.dashboard;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Builder
public class MovieTypeRevenueResponse {

    private List<MovieTypeRevenue> data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MovieTypeRevenue {
        private String movieType;
        private Long quantity;
        private BigDecimal revenue;
    }
}
