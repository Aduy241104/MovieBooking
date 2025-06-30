package com.example.demo.repository;

import com.example.demo.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {

    Promotion findAllByIsDeleted(Boolean isDeleted);

    Promotion findByCode(String code);

    Boolean existsByCode(String code);

    Long countByActive(Boolean active);

    List<Promotion> findByActiveTrue();
}
