package com.example.demo.DTO.request;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

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
    @NotBlank(message = "Full name is required")
    String fullName;

    @NotBlank(message = "Gender is required") 
    String gender;

    @Pattern(regexp = "^0\\d{9}$", message = "Invalid phone number format")
    String phoneNumber;

    
    String identityCard;

    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth;

    String avatar;
}
