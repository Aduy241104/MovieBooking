package com.example.demo.controller;

import com.example.demo.DTO.request.FareTypeRequest;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.FareTypeResponse;
import com.example.demo.DTO.response.ResPagination;
import com.example.demo.model.FareType;
import com.example.demo.service.FareTypeService;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/fare-types")
public class FareTypeController {

    private static final Logger logger = LoggerFactory.getLogger(FareTypeController.class);

    private final FareTypeService fareTypeService;

    public FareTypeController(FareTypeService fareTypeService) {
        this.fareTypeService = fareTypeService;
    }

    // Thêm mới FareType
    @PostMapping("/create")
    public ApiResponse<FareTypeResponse> createFareType(@RequestBody FareTypeRequest request) {
        FareType fareType = fareTypeService.handleCreateFareType(request);
        return ApiResponse.<FareTypeResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Thêm mới loại giá thành công")
                .result(convertToResponse(fareType))
                .build();
    }

    // Sửa FareType
    @PutMapping("/update/{id}")
    public ApiResponse<FareTypeResponse> updateFareType(@PathVariable Long id, @RequestBody FareTypeRequest request) {
        FareType fareType = fareTypeService.handleUpdateFareType(request, id);
        return ApiResponse.<FareTypeResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật loại giá thành công")
                .result(convertToResponse(fareType))
                .build();
    }

    // Xóa mềm FareType
    @PutMapping("/is-deleted/{id}")
    public ApiResponse<FareTypeResponse> deleteFareType(@PathVariable Long id) {
        logger.info("Gọi endpoint xóa mềm FareType với ID: {}", id);
        FareType fareType = fareTypeService.handleDeleteFareType(id);
        return ApiResponse.<FareTypeResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Xóa loại giá thành công")
                .result(convertToResponse(fareType))
                .build();
    }

    // Lấy danh sách FareType với phân trang và lọc
    @GetMapping
    public ApiResponse<ResPagination> getAllFareTypes(
            @Filter Specification<FareType> spec, Pageable pageable) {
        return ApiResponse.<ResPagination>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách loại giá thành công")
                .result(fareTypeService.fetchAllFareTypes(spec, pageable))
                .build();
    }

    // Lấy tất cả FareType chưa bị xóa mềm
    @GetMapping("/getAll")
    public ApiResponse<List<FareTypeResponse>> getAllActiveFareTypes() {
        List<FareType> fareTypes = fareTypeService.fetchFareTypeByIsDeletedFalse();
        List<FareTypeResponse> fareTypeResponses = fareTypes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<FareTypeResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy tất cả loại giá chưa xóa thành công")
                .result(fareTypeResponses)
                .build();
    }

    // Chuyển đổi từ Entity sang Response DTO
    private FareTypeResponse convertToResponse(FareType fareType) {
        FareTypeResponse response = new FareTypeResponse();
        BeanUtils.copyProperties(fareType, response);
        return response;
    }
}