package com.example.demo.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PaginatedResponseDTO<T> {
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private List<T> content;
}
