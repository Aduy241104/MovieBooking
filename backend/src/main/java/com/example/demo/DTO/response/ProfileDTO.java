package com.example.demo.DTO.response;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileDTO {
    Long accountId;
    String email;
    String fullName;
    String gender;
    String phoneNumber;
    String identityCard;
    LocalDate dateOfBirth;
    String avatar;
    Integer score;

}
