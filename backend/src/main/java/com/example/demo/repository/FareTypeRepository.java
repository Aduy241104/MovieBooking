package com.example.demo.repository;

import com.example.demo.model.FareType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FareTypeRepository extends JpaRepository<FareType, Long>, JpaSpecificationExecutor<FareType> {

    // Get all FareTypes that are not soft-deleted
    List<FareType> findByIsDeletedFalse();

    // Find FareType by name and not soft-deleted
    FareType findByNameAndIsDeletedFalse(String name);

    // Check existence by name
    Boolean existsByName(String name);

    // Find ticket type by name (case insensitive)
    FareType findByNameIgnoreCase(String name);

    // Find ticket type by age range
//    @Query("SELECT f FROM FareType f WHERE f.minAge <= :age AND f.maxAge >= :age AND f.isDeleted = false")
//    List<FareType> findByAgeRange(@Param("age") int age);

    // Find student ticket types
//    @Query("SELECT f FROM FareType f WHERE LOWER(f.name) LIKE %:keyword% AND f.isDeleted = false")
//    List<FareType> findByNameContainingIgnoreCaseAndIsDeletedFalse(@Param("keyword") String keyword);

    // Find discounted ticket types
//    @Query("SELECT f FROM FareType f WHERE f.discountPercent > 0 AND f.isDeleted = false")
//    List<FareType> findDiscountedFareTypes();
}
