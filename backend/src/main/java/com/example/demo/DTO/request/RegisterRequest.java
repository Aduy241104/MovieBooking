package com.example.demo.DTO.request;


import java.time.LocalDate;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password;

    @NotBlank(message = "Full name must not be blank")
    String fullName;

    @NotBlank(message = "Gender must not be blank")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    String gender;

    @NotBlank(message = "Phone number must not be blank")
    @Pattern(regexp = "^\\d{10,15}$", message = "Phone number must be 10 to 15 digits")
    String phoneNumber;

   
    String identityCard;

    @NotNull(message = "Date of birth must not be null")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth;
    
}
