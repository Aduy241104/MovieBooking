package com.example.demo.service;

import com.example.demo.DTO.request.FareTypeRequest;
import com.example.demo.DTO.response.FareTypeResponse;
import com.example.demo.DTO.response.ResPagination;
import com.example.demo.model.FareType;
import com.example.demo.repository.FareTypeRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FareTypeService {

    private static final Logger logger = LoggerFactory.getLogger(FareTypeService.class);

    private final FareTypeRepository fareTypeRepository;

    public FareTypeService(FareTypeRepository fareTypeRepository) {
        this.fareTypeRepository = fareTypeRepository;
    }

    // Thêm mới FareType
    public FareType handleCreateFareType(FareTypeRequest request) {
        FareType fareType = new FareType();
        BeanUtils.copyProperties(request, fareType);
        fareType.setIsDeleted(false);
        return fareTypeRepository.save(fareType);
    }

    // Sửa FareType
    public FareType handleUpdateFareType(FareTypeRequest request, Long id) {
        FareType currentFareType = fareTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại giá"));
        BeanUtils.copyProperties(request, currentFareType, "id", "isDeleted");
        return fareTypeRepository.save(currentFareType);
    }

    // Xóa mềm FareType
    @Transactional
    public FareType handleDeleteFareType(Long id) {
        logger.info("Bắt đầu xóa mềm FareType với ID: {}", id);
        FareType currentFareType = fareTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại giá với ID: " + id));
        if (currentFareType.getIsDeleted()) {
            throw new RuntimeException("Loại giá này đã bị xóa");
        }
        currentFareType.setIsDeleted(true);
        FareType updatedFareType = fareTypeRepository.save(currentFareType);
        logger.info("Xóa mềm thành công FareType với ID: {}, isDeleted: {}", id, updatedFareType.getIsDeleted());
        return updatedFareType;
    }

    // Lấy danh sách FareType với phân trang và lọc
    public ResPagination fetchAllFareTypes(Specification<FareType> spec, Pageable pageable) {
        Specification<FareType> finalSpec = Specification.where(spec)
                .and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("isDeleted"), false));

        Page<FareType> fareTypes = fareTypeRepository.findAll(finalSpec, pageable);

        ResPagination.MetaDTO metaDTO = ResPagination.MetaDTO.builder()
                .page(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .pages(fareTypes.getTotalPages())
                .total(fareTypes.getTotalElements())
                .build();

        return ResPagination.builder()
                .meta(metaDTO)
                .data(fareTypes.getContent().stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    // Tìm FareType theo ID
    public FareType fetchFareTypeById(Long id) {
        return fareTypeRepository.findById(id).orElse(null);
    }

    // Tìm FareType theo tên
    public FareType fetchFareTypeByName(String name) {
        return fareTypeRepository.findByNameAndIsDeletedFalse(name);
    }

    // Kiểm tra tồn tại theo tên
    public boolean existsByName(String name) {
        return fareTypeRepository.existsByName(name);
    }

    // Lấy tất cả FareType chưa bị xóa mềm
    public List<FareType> fetchFareTypeByIsDeletedFalse() {
        return fareTypeRepository.findByIsDeletedFalse();
    }

    // Chuyển đổi từ Entity sang Response DTO
    private FareTypeResponse convertToResponse(FareType fareType) {
        FareTypeResponse respond = new FareTypeResponse();
        BeanUtils.copyProperties(fareType, respond);
        return respond;
    }
}