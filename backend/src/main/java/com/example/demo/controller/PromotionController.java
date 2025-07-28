package com.example.demo.controller;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.ResPagination;
//import com.example.demo.model.Account;
import com.example.demo.model.Promotion;
import com.example.demo.service.PromotionService;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin")
public class PromotionController {
    @Autowired
    private PromotionService promotionService;

    @PostMapping("/promotions")
    public ResponseEntity<ApiResponse<Promotion>> createPromotion(@RequestBody Promotion promotion) {
        ApiResponse<Promotion> response = ApiResponse.<Promotion>builder()
                .status(HttpStatus.CREATED.value())
                .message("Create promotion successfully")
                .result(promotionService.handleCreatePromotion(promotion))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/promotions")
    public ResponseEntity<ApiResponse<Promotion>> updatePromotion(@RequestBody Promotion promotion) {
        ApiResponse<Promotion> response = ApiResponse.<Promotion>builder()
                .status(HttpStatus.OK.value())
                .message("Update promotion successfully")
                .result(promotionService.handleUpdatePromotion(promotion))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/promotions/active")
    public ResponseEntity<ApiResponse<Promotion>> updatePromotionActive(@RequestBody Promotion promotion) {
        ApiResponse<Promotion> response = ApiResponse.<Promotion>builder()
                .status(HttpStatus.OK.value())
                .message("Update promotion active status successfully")
                .result(promotionService.handleUpdatePromotionActive(promotion))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/promotions/is-deleted")
    public ResponseEntity<ApiResponse<Promotion>> deletePromotion(@RequestBody Promotion promotion) {
        ApiResponse<Promotion> response = ApiResponse.<Promotion>builder()
                .status(HttpStatus.OK.value())
                .message("Delete promotion successfully")
                .result(promotionService.handleDeletePromotion(promotion))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/promotions")
    public ResponseEntity<ApiResponse<ResPagination>> getAllPromotions(
            @Filter Specification<Promotion> spec, Pageable pageable) {
        ApiResponse<ResPagination> response = ApiResponse.<ResPagination>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch all promotion successfully")
                .result(promotionService.fetchAllPromotions(spec, pageable))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/promotions/total-active")
    public ResponseEntity<ApiResponse<Long>> countActivePromotions() {
        ApiResponse<Long> response = ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Get total active promotions successfully")
                .result(promotionService.getTotalActivePromotions())
                .build();
        return ResponseEntity.ok(response);
    }
}
