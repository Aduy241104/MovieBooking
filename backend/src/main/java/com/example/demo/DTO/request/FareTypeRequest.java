package com.example.demo.DTO.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FareTypeRequest {
    private String name;
    private BigDecimal basePrice;
    private BigDecimal dayPrice;
    private String timeSlotType;
    private String movieFormat;
}