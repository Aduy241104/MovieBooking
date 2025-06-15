package com.example.demo.DTO.response.dashboard;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class UserRegistrationsResponse {
    private List<UserRegistrations> data;

    @Setter
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRegistrations {
        private LocalDate date;
        private Long newUsers;
        private Long totalUsers;
    }
}
