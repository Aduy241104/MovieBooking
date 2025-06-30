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
}