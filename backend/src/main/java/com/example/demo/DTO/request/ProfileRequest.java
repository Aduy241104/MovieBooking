package com.example.demo.DTO.request;

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
public class ProfileRequest {
    String fullName;
    String gender;
    String phoneNumber;
    String identityCard;
    LocalDate dateOfBirth;
    String avatar;
    Integer score;
}
