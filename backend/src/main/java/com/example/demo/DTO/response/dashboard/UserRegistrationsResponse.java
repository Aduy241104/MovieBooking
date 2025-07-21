package com.example.demo.DTO.response.dashboard;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationsResponse {
    private LocalDate date;
    private Long newUsers;
    private Long totalUsers;
}
