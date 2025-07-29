package com.example.demo.service;

import com.example.demo.DTO.request.FareTypeRequest;
import com.example.demo.DTO.response.FareTypeResponse;
import com.example.demo.DTO.response.ResPagination;
import com.example.demo.exception.AppException;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.Account;
import com.example.demo.model.FareType;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.FareTypeRepository;
import com.example.demo.utils.SecurityUtils;
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

    public FareType handleCreateFareType(FareTypeRequest request) {
        FareType fareType = new FareType();
        BeanUtils.copyProperties(request, fareType);
        fareType.setIsDeleted(false);

        setLogAndNotification(
                fareType.getId(),
                "TẠO MỚI",
                "Tạo mới loại vé " + fareType.getName() + ", định dạng phim: " + fareType.getMovieFormat(),
                "Tạo mới loại vé",
                " vừa tạo mới loại vé: " + fareType.getName() + ", định dạng phim: " + fareType.getMovieFormat()
        );

        return fareTypeRepository.save(fareType);
    }

    public FareType handleUpdateFareType(FareTypeRequest request, Long id) {
        FareType currentFareType = fareTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy loại giá"));
        BeanUtils.copyProperties(request, currentFareType, "id", "isDeleted");

        setLogAndNotification(
                id,
                "CẬP NHẬT",
                "Cập nhật loại vé " + currentFareType.getName() + ", định dạng phim: " + currentFareType.getMovieFormat(),
                "Cập nhật loại vé",
                " vừa cập nhật loại vé: " + currentFareType.getName() + ", định dạng phim: " + currentFareType.getMovieFormat()
        );

        return fareTypeRepository.save(currentFareType);
    }

    @Transactional
    public FareType handleDeleteFareType(Long id) {
        logger.info("Bắt đầu xóa mềm FareType với ID: {}", id);
        FareType currentFareType = fareTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy loại giá với ID: " + id));

        if (Boolean.TRUE.equals(currentFareType.getIsDeleted())) {
            throw new BadRequestException("Loại giá này đã bị xóa trước đó");
        }

        currentFareType.setIsDeleted(true);
        FareType updatedFareType = fareTypeRepository.save(currentFareType);
        logger.info("Xóa mềm thành công FareType với ID: {}, isDeleted: {}", id, updatedFareType.getIsDeleted());

        setLogAndNotification(
                id,
                "XOÁ",
                "Xóa loại vé " + currentFareType.getName() + ", định dạng phim: " + currentFareType.getMovieFormat(),
                "Xoá loại vé",
                " vừa xóa loại vé: " + currentFareType.getName() + ", định dạng phim: " + currentFareType.getMovieFormat()
        );

        return updatedFareType;
    }

    public ResPagination fetchAllFareTypes(Specification<FareType> spec, Pageable pageable) {
        Specification<FareType> finalSpec = Specification.where(spec)
                .and((root, query, cb) -> cb.equal(root.get("isDeleted"), false));

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

    public FareType fetchFareTypeById(Long id) {
        return fareTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy loại giá với ID: " + id));
    }

    public FareType fetchFareTypeByName(String name) {
        FareType fareType = fareTypeRepository.findByNameAndIsDeletedFalse(name);
        if (fareType == null) {
            throw new NotFoundException("Không tìm thấy loại giá tên: " + name);
        }
        return fareType;
    }

    public boolean existsByName(String name) {
        return fareTypeRepository.existsByName(name);
    }

    public List<FareType> fetchFareTypeByIsDeletedFalse() {
        return fareTypeRepository.findByIsDeletedFalse();
    }

    private FareTypeResponse convertToResponse(FareType fareType) {
        FareTypeResponse respond = new FareTypeResponse();
        BeanUtils.copyProperties(fareType, respond);
        return respond;
    }

    private void setLogAndNotification(Long fareTypeId, String action, String description,
                                       String title, String content) {
        FareType currentFareType = getFareTypeOrThrow(fareTypeId);

        String loginUserId = SecurityUtils.getCurrentUsername();
        if (loginUserId == null || loginUserId.isEmpty()) {
            throw new UnauthorizedException("User chưa đăng nhập");
        }

        Account editorAccount = getAccountOrThrow(Long.valueOf(loginUserId));

        activityLogService.log(
                editorAccount.getEmail(),
                action,
                "LOẠI GIÁ",
                currentFareType.getName(),
                description
        );

        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        if (adminAccounts.isEmpty()) {
            throw new AppException("Không tìm thấy tài khoản admin để gửi thông báo");
        }

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

    public FareType getFareTypeOrThrow(Long fareTypeId) {
        return fareTypeRepository.findById(fareTypeId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy loại giá với ID: " + fareTypeId));
    }

    public Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản với ID: " + accountId));
    }
}
