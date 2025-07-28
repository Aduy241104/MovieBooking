package com.example.demo.service;


import com.example.demo.exception.DuplicateNameException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.exception.AppException;
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

    /**
     * Adds a new movie type if the name is unique.
     * Logs the activity and sends notifications to other admins.
     */
    public Type addType(Type type) {
        if (typeRepository.existsByName(type.getName())) {
            throw new DuplicateNameException("Thể loại đã tồn tại!");
        }

        // Log activity and notification
        setLogAndNotification(
                type,
                "TẠO MỚI",
                "Tạo mới thể loại phim",
                "Tạo mới thể loại phim",
                " vừa tạo mới thể loại phim: ");

        return typeRepository.save(type);
    }

    /**
     * Retrieves all movie types that have not been soft-deleted.
     */
    public List<Type> getAllTypes() {
        return typeRepository.findByIsDeletedFalse();
    }

    /**
     * Updates the name of an existing type if it exists and the new name is not duplicated.
     * Logs the activity and notifies other admins.
     */
    public Type updateType(Integer id, Type updatedType) {
        Type existing = typeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thể loại"));

        if (typeRepository.existsByNameAndIdNot(updatedType.getName(), id)) {
            throw new DuplicateNameException("Tên thể loại đã tồn tại!");
        }


        // Log activity and notification
        setLogAndNotification(
                existing,
                "CẬP NHẬT",
                "Cập nhật thể loại phim",
                "Cập nhật thể loại phim",
                " vừa cập nhật thể loại phim: ");

        existing.setName(updatedType.getName());
        return typeRepository.save(existing);
    }

    /**
     * Soft deletes the given type by setting the isDeleted flag to true.
     * Logs the deletion and notifies other admins.
     */
    public void deleteType(Integer id) {
        Type type = typeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thể loại để xóa!"));

        type.setIsDeleted(true);
        typeRepository.save(type);


        // Log activity and notification
        setLogAndNotification(
                type,
                "XOÁ",
                "Xoá thể loại phim",
                "Xoá thể loại phim",
                " vừa xoá thể loại phim: ");
    }

    /**
     * Fetches a type by its ID.
     */
    public Optional<Type> getTypeById(Integer id) {
        return typeRepository.findById(id);
    }

    /**
     * Helper method to log actions (create, update, delete) and send system notifications
     * to all other admin users except the acting user.
     */
    private void setLogAndNotification(Type type, String action, String description,
                                       String title, String content) {
        String loginUserId = SecurityUtils.getCurrentUsername();
        if (loginUserId == null || loginUserId.isEmpty()) {

            throw new AppException("User not logged in");
        }
        Account user = accountRepository.findById(Long.valueOf(loginUserId))
                .orElseThrow(() -> new AppException("Current user not found"));
        // Log the activity
        activityLogService.log(
                user.getEmail(),
                action,
                "THỂ LOẠI PHIM",
                type.getName(),
                description
        );

        // Send notification to the admin accounts
        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        if( adminAccounts.isEmpty()) {
            throw new AppException("No admin accounts found to notify");
        }
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
