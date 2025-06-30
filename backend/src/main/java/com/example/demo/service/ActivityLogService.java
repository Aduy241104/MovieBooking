package com.example.demo.service;


import com.example.demo.DTO.response.ResPagination;
import com.example.demo.model.ActivityLog;
import com.example.demo.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class ActivityLogService {
    @Autowired
    private ActivityLogRepository activityLogRepository;

    public void log(String updatedBy, String action, String entityType, String userUpdated, String description) {
        ActivityLog log = ActivityLog.builder()
                .updatedBy(updatedBy)
                .action(action)
                .entityType(entityType)
                .userUpdated(userUpdated)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        activityLogRepository.save(log);
    }

    public ResPagination fetchAllLogs(Specification<ActivityLog> specification, Pageable pageable) {
        Page<ActivityLog> activityLogPage = activityLogRepository.findAll(specification, pageable);
        ResPagination.MetaDTO metaDTO = ResPagination.MetaDTO.builder()
                .page(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .pages(activityLogPage.getTotalPages())
                .total(activityLogPage.getTotalElements())
                .build();
        return ResPagination.builder()
                .data(activityLogPage.getContent())
                .meta(metaDTO)
                .build();
    }
}
