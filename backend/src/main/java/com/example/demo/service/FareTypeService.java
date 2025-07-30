package com.example.demo.service;

import com.example.demo.DTO.request.FareTypeRequest;
import com.example.demo.DTO.response.FareTypeResponse;
import com.example.demo.DTO.response.ResPagination;
import com.example.demo.exception.AppException;
import com.example.demo.model.Account;
import com.example.demo.model.FareType;
import com.example.demo.model.Screening;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.FareTypeRepository;
import com.example.demo.utils.SecurityUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private FareTypeRepository fareTypeRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ActivityLogService activityLogService;
    @Autowired
    private NotificationService notificationService;

    // Thêm mới FareType
    public FareType handleCreateFareType(FareTypeRequest request) {
        FareType fareType = new FareType();
        BeanUtils.copyProperties(request, fareType);
        fareType.setIsDeleted(false);

        // Log and notify about the new fare type creation
        setLogAndNotification(
                fareType.getId(),
                "TẠO MỚI",
                "Tạo mới loại vé " + fareType.getName() + ", định dạng phim: " + fareType.getMovieFormat(),
                "Tạo mới loại vé",
                " vừa tạo mới loại vé: " + fareType.getName() + ", định dạng phim: " + fareType.getMovieFormat()
        );

        return fareTypeRepository.save(fareType);
    }

    // Sửa FareType
    public FareType handleUpdateFareType(FareTypeRequest request, Long id) {
        FareType currentFareType = fareTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại giá"));
        BeanUtils.copyProperties(request, currentFareType, "id", "isDeleted");

        // Log and notify about the fare type update
        setLogAndNotification(
                id,
                "CẬP NHẬT",
                "Cập nhật loại vé " + currentFareType.getName() + ", định dạng phim: " + currentFareType.getMovieFormat(),
                "Cập nhật loại vé",
                " vừa cập nhật loại vé: " + currentFareType.getName() + ", định dạng phim: " + currentFareType.getMovieFormat()
        );

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

        // Log and notify about the fare type deletion
        setLogAndNotification(
                id,
                "XOÁ",
                "Xóa loại vé " + currentFareType.getName() + ", định dạng phim: " + currentFareType.getMovieFormat(),
                "Xoá loại vé",
                " vừa xóa loại vé: " + currentFareType.getName() + ", định dạng phim: " + currentFareType.getMovieFormat()
        );

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

    /**
     * Sets log and sends notification for fare type actions.
     */
    private void setLogAndNotification(Long fareTypeId, String action, String description,
                                       String title, String content) {
        // Check if the screening exists
        FareType currentFareType = getFareTypeOrThrow(fareTypeId);
        // Get the current logged-in user
        String loginUserId = SecurityUtils.getCurrentUsername();
        if (loginUserId == null || loginUserId.isEmpty()) {
            throw new AppException("User not logged in!");
        }
        Account editorAccount = getAccountOrThrow(Long.valueOf(loginUserId));
        // Log the activity
        activityLogService.log(
                editorAccount.getEmail(),
                action,
                "LỊCH CHIẾU",
                currentFareType.getName(),
                description
        );
        // Find all admin accounts to notify
        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        if (adminAccounts.isEmpty()) {
            throw new AppException("No admin accounts found to notify");
        }
        // Send notifications to all admin accounts except the editor
        for (Account admin : adminAccounts) {
            if (!admin.getAccountId().equals(editorAccount.getAccountId())) {
                notificationService.notify(
                        admin,
                        title,
                        editorAccount.getFullName() + content + currentFareType.getName(),
                        "SYSTEM"
                );
            }
        }
    }

    /**
     * Find fare type by id, or throw AppException("Fare type not found").
     */
    public FareType getFareTypeOrThrow(Long fareTypeId) {
        return fareTypeRepository.findById(fareTypeId)
                .orElseThrow(() -> new AppException("Fare type not found"));
    }

    /**
     * Find account by id, or throw AppException("Account not found").
     */
    public Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException("Account not found"));
    }
}