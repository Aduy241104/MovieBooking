package com.example.demo.service;

import com.example.demo.DTO.response.ResPagination;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;


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

        String discountValue = formatDiscount(promotion.getDiscountLevel());

        setLogAndNotification(promotion, "TẠO MỚI",
                "Tạo mới mã khuyến mãi" + " với mức giảm giá: " + discountValue,
                "Tạo mới mã khuyến mãi",
                " vừa tạo mới khuyến mãi: ");

        return promotionRepository.save(promotion);
    }

    public Promotion handleUpdatePromotion(Promotion promotion) {
        Promotion currentPromotion = promotionRepository.findById(promotion.getId()).orElse(null);
        if (currentPromotion == null) {
            throw new RuntimeException("Promotion not found");
        }

        StringBuilder message = new StringBuilder();
        /*---- 1. Kiểm tra mã khuyến mãi thay đổi ----*/
        if (!Objects.equals(promotion.getCode(), currentPromotion.getCode())) {   // !=
            message.append("Mã KM: ")
                    .append(currentPromotion.getCode())
                    .append(" -> ")
                    .append(promotion.getCode())
                    .append(", ");
        }
        /*---- 2. Kiểm tra mức giảm giá thay đổi ----*/
        if (promotion.getDiscountLevel().compareTo(currentPromotion.getDiscountLevel()) != 0) { // !=
            String oldDiscount = formatDiscount(currentPromotion.getDiscountLevel());
            String newDiscount = formatDiscount(promotion.getDiscountLevel());

            message.append("Giảm giá: ")
                    .append(oldDiscount)
                    .append(" -> ")
                    .append(newDiscount)
                    .append(", ");
        }
        /*---- 3. Kiểm tra ngày bắt đầu thay đổi ----*/
        if (!promotion.getStartTime().isEqual(currentPromotion.getStartTime())) {          // !=
            String oldStart = formatDate(currentPromotion.getStartTime());
            String newStart = formatDate(promotion.getStartTime());

            message.append("Bắt đầu: ")
                    .append(oldStart)
                    .append(" -> ")
                    .append(newStart)
                    .append(", ");
        }
        /*---- 4. Kiểm tra ngày hết hạn thay đổi ----*/
        if (!promotion.getEndTime().isEqual(currentPromotion.getEndTime())) {              // !=
            String oldEnd = formatDate(currentPromotion.getEndTime());
            String newEnd = formatDate(promotion.getEndTime());

            message.append("Hết hạn: ")
                    .append(oldEnd)
                    .append(" -> ")
                    .append(newEnd)
                    .append(", ");
        }

        String description = "";
        if(message.isEmpty()) {
            description = "Cập nhật thông tin mã khuyến mãi";
        } else {
            description = "Cập nhật thông tin mã khuyến mãi: " + message;
        }

        setLogAndNotification(currentPromotion, "CẬP NHẬT",
                description,
                "Cập nhật thông tin khuyến mãi",
                " vừa cập nhật thông tin khuyến mãi: ");

        BeanUtils.copyProperties(promotion, currentPromotion, "id");

        return promotionRepository.save(currentPromotion);
    }

    public Promotion handleUpdatePromotionActive(Promotion promotion) {
        Promotion currentPromotion = promotionRepository.findById(promotion.getId()).orElse(null);
        if (currentPromotion == null) {
            throw new RuntimeException("Promotion not found");
        }
        currentPromotion.setActive(promotion.getActive());
        return promotionRepository.save(currentPromotion);
    }

    public Promotion handleDeletePromotion(Promotion promotion) {
        Promotion currentPromotion = promotionRepository.findById(promotion.getId()).orElse(null);
        if (currentPromotion == null) {
            throw new RuntimeException("Promotion not found");
        }
        currentPromotion.setIsDeleted(true);

        setLogAndNotification(currentPromotion, "XOÁ",
                "Xoá mã khuyến mãi" + currentPromotion.getCode(),
                "Xoá mã khuyến mãi",
                " vừa xoá mã khuyến mãi: ");

        return promotionRepository.save(currentPromotion);
    }

    public ResPagination fetchAllPromotions(Specification<Promotion> spec, Pageable pageable) {
        // Combine user specification with isDeleted = false condition
        Specification<Promotion> finalSpec = Specification.where(spec)
                .and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("isDeleted"), false));

        Page<Promotion> promotions = promotionRepository.findAll(finalSpec, pageable);

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

    public Long getTotalActivePromotions() {
        return promotionRepository.countByActive(true);
    }

    public Promotion fetchPromotionById(Long id) {
        return promotionRepository.findById(id).orElse(null);
    }

    public Promotion fetchPromotionByCode(String code) {
        return promotionRepository.findByCode(code);
    }

    public boolean existsByCode(String code) {
        return promotionRepository.existsByCode(code);
    }

    private void setLogAndNotification(Promotion currentPromotion, String action, String description,
                                       String title, String content) {
        String loginUserId = SecurityUtils.getCurrentUsername();
        if(loginUserId == null || loginUserId.isEmpty()) {
            throw new RuntimeException("Current user not found");
        }
        Account user = accountRepository.findById(Long.valueOf(loginUserId))
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Lưu log hoạt động cập nhật thông tin mã khuyến mãi
        // Ghi log hoạt động
        activityLogService.log(
                user.getEmail(),
                action,
                "KHUYẾN MÃI",
                currentPromotion.getCode(),
                description
        );

        // Gửi notification cho tất cả admin còn lại
        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        for (Account admin : adminAccounts) {
            if (!admin.getAccountId().equals(user.getAccountId())) {
                notificationService.notify(
                        admin,
                        title,
                        user.getFullName() + content + currentPromotion.getCode(),
                        "SYSTEM"
                );
            }
        }
    }

    /*------------------------------------------------
     * Hàm tiện ích: trả về chuỗi đã có % hoặc đ
     *------------------------------------------------*/
    private static String formatDiscount(BigDecimal level) {
        String suffix = (level.compareTo(BigDecimal.ONE) >= 0 &&
                level.compareTo(BigDecimal.valueOf(99)) <= 0)
                ? "%"
                : "đ";
        return level.stripTrailingZeros().toPlainString() + suffix;
    }

    /*------------------------------------------------
     * Hàm định dạng ngày/giờ
     *------------------------------------------------*/
    private static String formatDate(LocalDateTime time) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return time.format(fmt);
    }
}
