package com.example.demo.DTO.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpTokenDTO {
    private Long accountID;
    private String email;
}
