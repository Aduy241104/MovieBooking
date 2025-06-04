package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.MetaDTO;
import com.example.demo.DTO.response.SingleMovieDTO;
import com.example.demo.service.FindMovieService;
import com.example.demo.model.Movie;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequestMapping("/api/public/findMovie")
public class FindMovieController {

    @Autowired
    FindMovieService findMovieService;

    @GetMapping("/getAll")
    public ApiResponse<List<Movie>> getMethodName() {
        List<Movie> response = findMovieService.getAllMovie();
        return ApiResponse.<List<Movie>>builder()
                .message("Success")
                .result(response)
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<Map<String, Object>> searchMoviess(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {

        if (page < 0)
            page = 0;
        if (size <= 0)
            size = 4;

        Page<SingleMovieDTO> result = findMovieService.search(keyword.replace("\"", ""), page, size);

        MetaDTO metaDTO = MetaDTO.builder()
                .count(result.getNumberOfElements())
                .current_page(result.getNumber() + 1)
                .per_page(result.getSize())
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("Movie", result.getContent());
        response.put("Meta", metaDTO);
        return ApiResponse.<Map<String, Object>>builder()
                .message("successful")
                .result(response)
                .build();
    }

}
