package com.example.demo.repository;

import com.example.demo.model.FareType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FareTypeRepository extends JpaRepository<FareType, Long>, JpaSpecificationExecutor<FareType> {

    // Lấy tất cả FareType chưa bị xóa mềm
    List<FareType> findByIsDeletedFalse();

    // Tìm FareType theo tên và chưa bị xóa mềm
    FareType findByNameAndIsDeletedFalse(String name);

    // Kiểm tra tồn tại theo tên
    Boolean existsByName(String name);

    // Tìm loại vé theo tên
    FareType findByNameIgnoreCase(String name);

    // Tìm loại vé theo độ tuổi
//    @Query("SELECT f FROM FareType f WHERE f.minAge <= :age AND f.maxAge >= :age AND f.isDeleted = false")
//    List<FareType> findByAgeRange(@Param("age") int age);

    // Tìm loại vé học sinh/sinh viên
//    @Query("SELECT f FROM FareType f WHERE LOWER(f.name) LIKE %:keyword% AND f.isDeleted = false")
//    List<FareType> findByNameContainingIgnoreCaseAndIsDeletedFalse(@Param("keyword") String keyword);

    // Tìm loại vé có giảm giá
//    @Query("SELECT f FROM FareType f WHERE f.discountPercent > 0 AND f.isDeleted = false")
//    List<FareType> findDiscountedFareTypes();
}