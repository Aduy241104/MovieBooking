package com.example.demo.DTO.request;


import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
// @JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterRequest {
    String email;
    String password;
    String fullName;
    String gender;
    String phoneNumber;
    String identityCard;
    LocalDate dateOfBirth;
    
}
