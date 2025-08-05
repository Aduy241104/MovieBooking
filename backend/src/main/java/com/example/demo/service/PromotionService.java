package com.example.demo.service;

import com.example.demo.DTO.response.ResPagination;
import com.example.demo.exception.AppException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Account;
import com.example.demo.model.Promotion;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.utils.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ActivityLogService activityLogService;
    @Autowired
    private NotificationService notificationService;

    public Promotion handleCreatePromotion(Promotion promotion) {
        // Check if the promotion code already exists
        assertPromotionCodeNotInUse(promotion.getCode(), null);
        // Format discount value by discount type (% or VND)
        String discountValue = formatDiscount(promotion.getDiscountLevel());
        // Save promotion trước để sinh id
        Promotion savedPromotion = promotionRepository.save(promotion);
        // Log the activity
        setLogAndNotification(
                savedPromotion.getId(),
                "TẠO MỚI",
                "Tạo mới mã khuyến mãi" + " với mức giảm giá: " + discountValue,
                "Tạo mới mã khuyến mãi",
                " vừa tạo mới khuyến mãi: ");
        return savedPromotion;
    }

    public Promotion handleUpdatePromotion(Promotion promotion) {
        // Check if the promotion exists
        Promotion currentPromotion = getPromotionOrThrow(promotion.getId());
        // Check if the promotion code is already in use by another promotion
        assertPromotionCodeNotInUse(promotion.getCode(), currentPromotion.getId());
        // Instead of using a mapper, use BeanUtils to copy properties
        // Update the properties of the existing promotion except for the promotion ID
        BeanUtils.copyProperties(promotion, currentPromotion, "id");
        // Log the activity
        setLogAndNotification(
                currentPromotion.getId(),
                "CẬP NHẬT",
                "Cập nhật mã thông tin khuyến mãi",
                "Cập nhật thông tin khuyến mãi",
                " vừa cập nhật thông tin khuyến mãi: ");
        return promotionRepository.save(currentPromotion);
    }

    public Promotion handleUpdatePromotionActive(Promotion promotion) {
        // Check if the promotion exists
        Promotion currentPromotion = getPromotionOrThrow(promotion.getId());
        // Update the active status of the promotion
        currentPromotion.setActive(promotion.getActive());
        return promotionRepository.save(currentPromotion);
    }

    public Promotion handleDeletePromotion(Promotion promotion) {
        // Check if the promotion exists
        Promotion currentPromotion = getPromotionOrThrow(promotion.getId());
        // Set the promotion as deleted
        currentPromotion.setIsDeleted(true);
        // Log the activity
        setLogAndNotification(
                currentPromotion.getId(),
                "XOÁ",
                "Xoá mã khuyến mãi" + currentPromotion.getCode(),
                "Xoá mã khuyến mãi",
                " vừa xoá mã khuyến mãi: ");
        return promotionRepository.save(currentPromotion);
    }

    /**
     * Fetch all promotions with pagination and filtering.
     *
     * @param spec     Specification for filtering promotions.
     * @param pageable Pageable object for pagination.
     * @return ResPagination containing the list of promotions and pagination
     *         metadata.
     */
    public ResPagination fetchAllPromotions(Specification<Promotion> spec, Pageable pageable) {
        // Combine user specification with isDeleted = false condition
        Specification<Promotion> finalSpec = Specification
                .where(spec)
                .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isDeleted"), false));
        // Fetch all promotions with pagination
        Page<Promotion> promotions = promotionRepository.findAll(finalSpec, pageable);
        // Create MetaDTO for pagination
        ResPagination.MetaDTO metaDTO = ResPagination.MetaDTO.builder()
                .page(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .pages(promotions.getTotalPages())
                .total(promotions.getTotalElements())
                .build();
        return ResPagination.builder()
                .meta(metaDTO)
                .data(promotions.getContent())
                .build();
    }

    /**
     * Get total number of active promotions.
     *
     * @return Total number of active promotions.
     */
    public Long getTotalActivePromotions() {
        return promotionRepository.countByActive(true);
    }

    /**
     * Set log and notification for promotion actions.
     *
     * @return void
     */
    private void setLogAndNotification(Long promotionId, String action, String description,
            String title, String content) {
        // Check if the promotion exists
        Promotion currentPromotion = getPromotionOrThrow(promotionId);
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
                "KHUYẾN MÃI",
                currentPromotion.getCode(),
                description);
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
                        editorAccount.getFullName() + content + currentPromotion.getCode(),
                        "SYSTEM");
            }
        }
    }

    /**
     * Get promotion by ID or throw AppException("Promotion not found").
     *
     * @param promotionId ID of the promotion.
     * @return Promotion object if found.
     */
    public Promotion getPromotionOrThrow(Long promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException("Promotion not found"));
    }

    /**
     * Check if promotion code exists.
     *
     * @param promotionCode Promotion code to check.
     */
    public void isPromotionExist(String promotionCode) {
        boolean exists = promotionRepository.existsByCode(promotionCode);
        if (exists) {
            throw new AppException("Promotion code already exists");
        }
    }

    /**
     * Check if promotion code is not in use.
     * If currentPromotionId is null, it means adding a new promotion.
     * If currentPromotionId is not null, it means updating an existing promotion.
     *
     * @param promotionCode      Promotion code to check.
     * @param currentPromotionId Current promotion ID (null for new promotions).
     */
    public void assertPromotionCodeNotInUse(String promotionCode, Long currentPromotionId) {
        boolean exists;
        if (currentPromotionId == null) {
            // Add new promotion: check if promotion code exists
            exists = promotionRepository.existsByCode(promotionCode);
        } else {
            // Update promotion: check if promotion code exists but not for the current
            // promotion
            exists = promotionRepository.existsByCodeAndIdNot(promotionCode, currentPromotionId);
        }
        if (exists) {
            throw new AppException("Promotion code already in use");
        }
    }

    /**
     * Format discount value based on the discount type.
     * If discount level is between 1 and 99, append '%' else append 'đ'.
     *
     * @param level Discount level.
     * @return Formatted discount string.
     */
    private static String formatDiscount(BigDecimal level) {
        String suffix = (level.compareTo(BigDecimal.ONE) >= 0 &&
                level.compareTo(BigDecimal.valueOf(99)) <= 0)
                        ? "%"
                        : "đ";
        return level.stripTrailingZeros().toPlainString() + suffix;
    }

    /**
     * Get account by ID or throw AppException("Account not found").
     *
     * @param accountId ID of the account.
     * @return Account object if found.
     */
    public Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException("Account not found"));
    }

    // Use for booking------------------------
    /**
     * @param code Promotional code needs to be checked.
     * @return Promotion object if valid.
     * @throws NotFoundException If the code does not exist, is expired, or is not
     *                           active.
     */
    public Promotion findAndValidatePromotion(String code) {
        Promotion promotion = promotionRepository.findByCode(code);

        if (promotion == null ||
                !promotion.getActive() ||
                promotion.getIsDeleted() ||
                LocalDateTime.now().isBefore(promotion.getStartTime()) ||
                LocalDateTime.now().isAfter(promotion.getEndTime())) {

            throw new NotFoundException("Mã khuyến mãi không hợp lệ hoặc đã hết hạn.");
        }

        return promotion;
    }

    public Promotion fetchPromotionByCode(String code) {
        Promotion promotion = promotionRepository.findByCode(code);
        if (promotion == null) {
            throw new AppException("Promotion not found");
        }
        return promotion;
    }
}
