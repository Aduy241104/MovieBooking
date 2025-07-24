package com.example.demo.DTO.response.dashboard;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationsResponse {
    private LocalDate date;
    private Long newUsers;
    private Long totalUsers;

    // // Constructor for @AllArgsConstructor functionality
    // public UserRegistrationsResponse(LocalDate date, Long newUsers, Long
    // totalUsers) {
    // this.date = date;
    // this.newUsers = newUsers != null ? newUsers : 0L;
    // this.totalUsers = totalUsers != null ? totalUsers : 0L;
    // }
    //
    // // Constructor for JPQL with LocalDateTime conversion
    // public UserRegistrationsResponse(LocalDateTime dateTime, Long newUsers, Long
    // totalUsers) {
    // this.date = dateTime != null ? dateTime.toLocalDate() : null;
    // this.newUsers = newUsers != null ? newUsers : 0L;
    // this.totalUsers = totalUsers != null ? totalUsers : 0L;
    // }

}
