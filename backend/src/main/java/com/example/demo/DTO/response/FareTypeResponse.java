package com.example.demo.DTO.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FareTypeResponse {
    private Long id;
    private String name;
    private BigDecimal basePrice;
    private BigDecimal dayPrice;
    private String timeSlotType;
    private String movieFormat;
}