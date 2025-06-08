package com.example.demo.controller;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.ResPagination;
import com.example.demo.model.Account;
import com.example.demo.model.Promotion;
import com.example.demo.service.PromotionService;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class PromotionController {
    private final PromotionService promotionService;
    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping("public/promotions")
    public ApiResponse<Promotion> createPromotion(@RequestBody Promotion promotion) {
        Promotion currentPromotion = promotionService.fetchPromotionByCode(promotion.getCode());
        if(currentPromotion != null) {
            throw new RuntimeException("Promotion already exists");
        }
        return ApiResponse.<Promotion>builder()
                .status(HttpStatus.CREATED.value())
                .message("Create promotion")
                .result(promotionService.handleCreatePromotion(promotion))
                .build();
    }

    @PutMapping("public/promotions")
    public ApiResponse<Promotion> updatePromotion(@RequestBody Promotion promotion) {
        Promotion currentPromotion = promotionService.fetchPromotionById(promotion.getId());
        if(currentPromotion == null) {
            throw new RuntimeException("Promotion not found");
        }
        // USER THAY ĐỔI MÃ CODE && KHÔNG TRÙNG CODE TRONG DATABASE => OK
        // USER KHÔNG THAY ĐỔI MÃ CODE => CŨNG OK VÌ VẾ SAU SẼ LUÔN SAI
        if (!currentPromotion.getCode().equals(promotion.getCode()) &&
                promotionService.existsByCode(promotion.getCode())) {
            throw new RuntimeException("Promotion already exists");
        }
        return ApiResponse.<Promotion>builder()
                .status(HttpStatus.OK.value())
                .message("Update promotion")
                .result(promotionService.handleUpdatePromotion(promotion))
                .build();
    }

    @PutMapping("public/promotions/active")
    public ApiResponse<Promotion> updatePromotionActive(@RequestBody Promotion promotion) {
        Promotion currentPromotion = promotionService.fetchPromotionById(promotion.getId());
        if(currentPromotion == null) {
            throw new RuntimeException("Promotion not found");
        }
        return ApiResponse.<Promotion>builder()
                .status(HttpStatus.OK.value())
                .message("Update promotion active")
                .result(promotionService.handleUpdatePromotionActive(promotion))
                .build();
    }

    @PutMapping("public/promotions/is-deleted")
    public ApiResponse<Promotion> deletePromotion(@RequestBody Promotion promotion) {
        Promotion currentPromotion = promotionService.fetchPromotionById(promotion.getId());
        if(currentPromotion == null) {
            throw new RuntimeException("Promotion not found");
        }
        return ApiResponse.<Promotion>builder()
                .status(HttpStatus.OK.value())
                .message("Delete promotion")
                .result(promotionService.handleDeletePromotion(promotion))
                .build();
    }

    @GetMapping("/public/promotions")
    public ApiResponse<ResPagination> getAllPromotions(
            @Filter Specification<Promotion> spec, Pageable pageable) {
        return ApiResponse.<ResPagination>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch all promotion")
                .result(promotionService.fetchAllPromotions(spec, pageable))
                .build();
    }
}
