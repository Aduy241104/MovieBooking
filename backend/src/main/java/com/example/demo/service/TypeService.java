package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.Type;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TypeRepository;
import com.example.demo.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TypeService {

    @Autowired
    private TypeRepository typeRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ActivityLogService activityLogService;
    @Autowired
    private NotificationService notificationService;

    public Type addType(Type type) {
        if (typeRepository.existsByName(type.getName())) {
            throw new RuntimeException("Thể loại đã tồn tại!");
        }
        // Log activity and notification
        setLogAndNotification(type, "TẠO MỚI",
                "Tạo mới thể loại phim",
                "Tạo mới thể loại phim",
                " vừa tạo mới thể loại phim: ");

        return typeRepository.save(type);
    }

    public List<Type> getAllTypes() {
        return typeRepository.findByIsDeletedFalse();
    }

    public Type updateType(Integer id, Type updatedType) {
        Type existing = typeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));

        // Kiểm tra tên trùng với thể loại khác
        if (typeRepository.existsByNameAndIdNot(updatedType.getName(), id)) {
            throw new RuntimeException("Tên thể loại đã tồn tại!");
        }

        // Log activity and notification
        setLogAndNotification(existing, "CẬP NHẬT",
                "Cập nhật thể loại phim",
                "Cập nhật thể loại phim",
                " vừa cập nhật thể loại phim: ");

        existing.setName(updatedType.getName());
        return typeRepository.save(existing);
    }

    public void deleteType(Integer id) {
        Optional<Type> typeOpt = typeRepository.findById(id);
        if (typeOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy thể loại để xóa!");
        }
        Type type = typeOpt.get();
        type.setIsDeleted(true);
        typeRepository.save(type);

        // Log activity and notification
        setLogAndNotification(type, "XOÁ",
                "Xoá thể loại phim",
                "Xoá thể loại phim",
                " vừa xoá thể loại phim: ");
    }

    public Optional<Type> getTypeById(Integer id) {
        return typeRepository.findById(id);
    }

    private void setLogAndNotification(Type type, String action, String description,
                                       String title, String content) {
        String loginUserId = SecurityUtils.getCurrentUsername();
        if (loginUserId == null || loginUserId.isEmpty()) {
            throw new RuntimeException("Current user not found");
        }
        Account user = accountRepository.findById(Long.valueOf(loginUserId))
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Lưu log hoạt động cập nhật thông tin mã khuyến mãi
        // Ghi log hoạt động
        activityLogService.log(
                user.getEmail(),
                action,
                "THỂ LOẠI PHIM",
                type.getName(),
                description
        );

        // Gửi notification cho tất cả admin còn lại
        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        for (Account admin : adminAccounts) {
            if (!admin.getAccountId().equals(user.getAccountId())) {
                notificationService.notify(
                        admin,
                        title,
                        user.getFullName() + content + type.getName(),
                        "SYSTEM"
                );
            }
        }
    }
}