package com.example.demo.service;

import java.util.List;

import com.example.demo.DTO.response.SingleMovieDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.model.Movie;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.TypeRepository;
import org.springframework.data.domain.Page;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FindMovieService {

    @Autowired
    MovieRepository movieRepository;

      @Autowired
    private TypeRepository typeRepository;

    public List<Movie> findMovieByName(String keyword) {
        List<Movie> response = movieRepository.findByNameVNContainingIgnoreCaseAndIsDeletedFalse(keyword);
        return response;
    }

    public List<Movie> getAllMovie() {
        List<Movie> response = movieRepository.findAll();
        return response;
    }
    
     public Page<SingleMovieDTO> search(String keyword, int page, int size) {
        Page<SingleMovieDTO> pageResult = movieRepository
                .searchMoviesWithRating(keyword, PageRequest.of(page, size));
        // Gán thể loại phim vào DTO
        pageResult.getContent().forEach(dto -> {
            List<String> types = typeRepository.findTypeNamesByMovieId(dto.getId());
            dto.setTypes(types);
        });

        return pageResult;
    }
}
