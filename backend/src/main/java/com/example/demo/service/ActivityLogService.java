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

    /**
     * Logs an activity with the specified details.
     *
     * @param updatedBy    The user who performed the action.
     * @param action       The action performed (e.g., "CREATE", "UPDATE", "DELETE").
     * @param entityType   The type of entity affected (e.g., "Account", "Movie").
     * @param userUpdated  The user affected by the action.
     * @param description  A description of the action.
     */
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

    /**
     * Fetches all activity logs with pagination and filtering.
     *
     * @param specification The specification for filtering logs.
     * @param pageable      The pagination information.
     * @return A paginated response containing the activity logs.
     */
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
