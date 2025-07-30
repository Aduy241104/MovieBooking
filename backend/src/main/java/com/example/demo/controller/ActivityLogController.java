package com.example.demo.controller;


import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.ResPagination;
import com.example.demo.model.ActivityLog;
import com.example.demo.service.ActivityLogService;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activity-logs")
public class ActivityLogController {
    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<ResPagination>> getAllLogs(
            @Filter Specification<ActivityLog> spec, Pageable pageable
    ) {
        ApiResponse<ResPagination> response = ApiResponse.<ResPagination>builder()
                .status(200)
                .message("Fetch all activity logs successfully")
                .result(activityLogService.fetchAllLogs(spec, pageable))
                .build();
        return ResponseEntity.ok(response);
    }
}
