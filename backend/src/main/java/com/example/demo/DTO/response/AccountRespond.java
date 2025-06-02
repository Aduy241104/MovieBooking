package com.example.demo.DTO.response;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountRespond {
    Long accountID;
    String email;
    String fullName;
    String gender;
    String phoneNumber;
    String identityCard;
    LocalDate dateOfBirth;
    LocalDate registerDate;
    Integer score;
    String role;
}
