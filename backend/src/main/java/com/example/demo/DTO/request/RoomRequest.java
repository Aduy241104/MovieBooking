package com.example.demo.DTO.request;

import lombok.Data;

import java.util.List;

import com.example.demo.DTO.response.SeatResponse;

@Data
public class RoomRequest {
    private String name;
    private int rows;
    private int cols;
    private List<SeatResponse> seats;
}
