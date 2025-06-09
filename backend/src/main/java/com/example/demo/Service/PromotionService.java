package com.example.demo.service;

import com.example.demo.DTO.response.ResPagination;
import com.example.demo.model.Promotion;
import com.example.demo.repository.PromotionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

//import java.util.List;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;
    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public Promotion handleCreatePromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    public Promotion handleUpdatePromotion(Promotion promotion) {
        Promotion currentPromotion = promotionRepository.findById(promotion.getId()).orElse(null);
        if (currentPromotion == null) {
            throw new RuntimeException("Promotion not found");
        }
        BeanUtils.copyProperties(promotion, currentPromotion, "id");
        return promotionRepository.save(currentPromotion);
    }

    public Promotion handleUpdatePromotionActive(Promotion promotion) {
        Promotion currentPromotion = promotionRepository.findById(promotion.getId()).orElse(null);
        if (currentPromotion == null) {
            throw new RuntimeException("Promotion not found");
        }
        currentPromotion.setActive(promotion.getActive());
        return promotionRepository.save(currentPromotion);
    }

    public Promotion handleDeletePromotion(Promotion promotion) {
        Promotion currentPromotion = promotionRepository.findById(promotion.getId()).orElse(null);
        if (currentPromotion == null) {
            throw new RuntimeException("Promotion not found");
        }
        currentPromotion.setIsDeleted(true);
        return promotionRepository.save(currentPromotion);
    }

    public ResPagination fetchAllPromotions(Specification<Promotion> spec, Pageable pageable) {
        // Combine user specification with isDeleted = false condition
        Specification<Promotion> finalSpec = Specification.where(spec)
                .and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("isDeleted"), false));

        Page<Promotion> promotions = promotionRepository.findAll(finalSpec, pageable);

        ResPagination.MetaDTO metaDTO = ResPagination.MetaDTO.builder()
                .page(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .pages(promotions.getTotalPages())
                .total(promotions.getTotalElements())
                .build();

        return ResPagination.builder()
                .meta(metaDTO)
                .data(promotions.getContent())
                .build();
    }

    public Promotion fetchPromotionById(Long id) {
        return promotionRepository.findById(id).orElse(null);
    }

    public Promotion fetchPromotionByCode(String code) {
        return promotionRepository.findByCode(code);
    }

    public boolean existsByCode(String code) {
        return promotionRepository.existsByCode(code);
    }
}
