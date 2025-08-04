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
@RequestMapping("/api/admin/fare-types")
public class FareTypeController {

    private static final Logger logger = LoggerFactory.getLogger(FareTypeController.class);

    private final FareTypeService fareTypeService;

    public FareTypeController(FareTypeService fareTypeService) {
        this.fareTypeService = fareTypeService;
    }

    // Create a new FareType
    @PostMapping("/create")
    public ApiResponse<FareTypeResponse> createFareType(@RequestBody FareTypeRequest request) {
        FareType fareType = fareTypeService.handleCreateFareType(request);
        return ApiResponse.<FareTypeResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Fare type created successfully")
                .result(convertToResponse(fareType))
                .build();
    }

    // Update an existing FareType
    @PutMapping("/update/{id}")
    public ApiResponse<FareTypeResponse> updateFareType(@PathVariable Long id, @RequestBody FareTypeRequest request) {
        FareType fareType = fareTypeService.handleUpdateFareType(request, id);
        return ApiResponse.<FareTypeResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Fare type updated successfully")
                .result(convertToResponse(fareType))
                .build();
    }

    // Soft delete a FareType
    @PutMapping("/is-deleted/{id}")
    public ApiResponse<FareTypeResponse> deleteFareType(@PathVariable Long id) {
        logger.info("Calling soft delete FareType endpoint with ID: {}", id);
        FareType fareType = fareTypeService.handleDeleteFareType(id);
        return ApiResponse.<FareTypeResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Fare type deleted successfully")
                .result(convertToResponse(fareType))
                .build();
    }

    // Get paginated and filtered list of FareTypes
    @GetMapping
    public ApiResponse<ResPagination> getAllFareTypes(
            @Filter Specification<FareType> spec, Pageable pageable) {
        return ApiResponse.<ResPagination>builder()
                .status(HttpStatus.OK.value())
                .message("Fare type list retrieved successfully")
                .result(fareTypeService.fetchAllFareTypes(spec, pageable))
                .build();
    }

    // Get all FareTypes that are not soft-deleted
    @GetMapping("/getAll")
    public ApiResponse<List<FareTypeResponse>> getAllActiveFareTypes() {
        List<FareType> fareTypes = fareTypeService.fetchFareTypeByIsDeletedFalse();
        List<FareTypeResponse> fareTypeResponses = fareTypes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<FareTypeResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("All active fare types retrieved successfully")
                .result(fareTypeResponses)
                .build();
    }

    // Convert Entity to Response DTO
    private FareTypeResponse convertToResponse(FareType fareType) {
        FareTypeResponse response = new FareTypeResponse();
        BeanUtils.copyProperties(fareType, response);
        return response;
    }
}
