package com.example.demo.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.demo.DTO.response.ResPagination;
import com.example.demo.DTO.response.dashboard.UserRegistrationsResponse;
import com.example.demo.exception.AppException;
import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import com.example.demo.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ActivityLogService activityLogService;
    @Autowired
    private NotificationService notificationService;

    public List<Account> getAllAccount() {
        return accountRepository.findAll();
    }

    public ResPagination fetchAllAccountPagination(Specification<Account> spec, Pageable pageable) {
        Specification<Account> finalSpec = Specification.where(spec)
                .and((root, query, criteriaBuilder)
                        -> criteriaBuilder.equal(root.get("isDeleted"), false));

        Page<Account> accountPage = accountRepository.findAll(finalSpec, pageable);
        ResPagination.MetaDTO metaDTO = ResPagination.MetaDTO.builder()
                .page(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .pages(accountPage.getTotalPages())
                .total(accountPage.getTotalElements())
                .build();
        return ResPagination.builder()
                .meta(metaDTO)
                .data(accountPage.getContent())
                .build();
    }

    public Account fetchAccountById(Long accountId) {
        // If accountId is null, throw an exception
        return getAccountOrThrow(accountId);
    }

    public Account handleCreateAccount(Account account) {
        Role role = roleRepository.findById(account.getRole().getRoleId())
                .orElseThrow(() -> new AppException("Role not found"));
        account.setRole(role);
        // Check if email already exists
        isAccountExist(account.getEmail());
        // Check phone number
        assertPhoneNotInUse(account.getPhoneNumber(), null);
        // Save the account in database
        Account savedAccount = accountRepository.save(account);
        // Set log and notification for account deletion
        setLogAndNotification(
                savedAccount.getAccountId(),
                "TẠO MỚI",
                "Tạo mới tài khoản (" + role.getRoleName() + ")",
                "Tạo mới tài khoản",
                " vừa tạo mới tài khoản: ");
        return savedAccount;
    }

    public Account handleUpdateAccountQuick(Account account) {
        // Check if account exists
        Account currentAccount = getAccountOrThrow(account.getAccountId());
        // Check phone if it is already in use by another account
        assertPhoneNotInUse(account.getPhoneNumber(), currentAccount.getAccountId());

        currentAccount.setFullName(account.getFullName());
        currentAccount.setGender(account.getGender());
        currentAccount.setDateOfBirth(account.getDateOfBirth());
        currentAccount.setPhoneNumber(account.getPhoneNumber());

        // Set log and notification for account deletion
        setLogAndNotification(
                currentAccount.getAccountId(),
                "CẬP NHẬT",
                "Cập nhật thông tin tài khoản",
                "Cập nhật tài khoản",
                " vừa cập nhật thông tin tài khoản: ");
        return accountRepository.save(currentAccount);
    }

    public Account handleUpdateAccountInfo(Long accountId, Account account) {
        // Check if account exists
        Account currentAccount = getAccountOrThrow(accountId);
        // Check phone if it is already in use by another account
        assertPhoneNotInUse(account.getPhoneNumber(), currentAccount.getAccountId());

        currentAccount.setFullName(account.getFullName());
        currentAccount.setGender(account.getGender());
        currentAccount.setDateOfBirth(account.getDateOfBirth());
        currentAccount.setPhoneNumber(account.getPhoneNumber());
        currentAccount.setIdentityCard(account.getIdentityCard());
        currentAccount.setScore(account.getScore());

        // Set log and notification for account deletion
        setLogAndNotification(
                currentAccount.getAccountId(),
                "CẬP NHẬT",
                "Cập nhật thông tin tài khoản",
                "Cập nhật tài khoản",
                " vừa cập nhật thông tin tài khoản: ");
        return accountRepository.save(currentAccount);
    }

    public Account handleUpdateStatusAccount(Account account) {
        // Check if account exists
        Account currentAccount = getAccountOrThrow(account.getAccountId());
        // Set new status for account
        currentAccount.setStatus(account.getStatus());
        return accountRepository.save(currentAccount);
    }

    public Account handleDeleteAccount(@RequestBody Account account) {
        // Check if account exists
        Account currentAccount = getAccountOrThrow(account.getAccountId());
        // Set account as deleted
        currentAccount.setIsDeleted(true);
        // Set log and notification for account deletion
        setLogAndNotification(
                currentAccount.getAccountId(),
                "XÓA",
                "Xóa tài khoản",
                "Xóa tài khoản",
                " đã xóa tài khoản: ");
        return accountRepository.save(currentAccount);
    }

    public Account handleUploadAvatar(Long accountId, MultipartFile avatarFile) {
        Account currentAccount = getAccountOrThrow(accountId);
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new AppException("No file uploaded");
        }
        try {
            // File save path, ex: src/main/resources/static/avatars/
            String uploadDir = "uploads/avatars/";
            // Create directory if it does not exist
            Files.createDirectories(Paths.get(uploadDir));
            // Create unique file names to avoid duplicates
            String fileName = "avatar_" + currentAccount.getAccountId() + "_" + System.currentTimeMillis() + "_"
                    + avatarFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, avatarFile.getBytes());
            // Update avatar field in account
            currentAccount.setAvatar(fileName);
            // Set log and notification for account deletion
            setLogAndNotification(
                    currentAccount.getAccountId(),
                    "CẬP NHẬT",
                    "Cập nhật avatar tài khoản",
                    "Cập nhật avatar tài khoản",
                    " vừa cập nhật avatar tài khoản: ");
            return accountRepository.save(currentAccount);
        } catch (Exception e) {
            throw new AppException("Failed to upload avatar: " + e.getMessage());
        }
    }

    public long getTotalAccountByRole(String roleName) {
        boolean isRoleExist = roleRepository.existsByRoleName(roleName);
        if (!isRoleExist) {
            throw new AppException("Role not found");
        }
        return accountRepository.countByRole_RoleName(roleName);
    }

    public List<UserRegistrationsResponse> getUserRegistrationsDTO(int monthCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate fromDate = now.minusMonths(monthCount).withDayOfMonth(1).toLocalDate();
        LocalDate toDate = now.withDayOfMonth(1).toLocalDate();
        // Get monthly registered user statistics
        List<UserRegistrationsResponse> stats = accountRepository.getUserRegistrationsByMonth(fromDate, toDate);
        // Convert list to Map for easy lookup by date
        Map<LocalDate, UserRegistrationsResponse> map = stats.stream()
                .collect(Collectors.toMap(UserRegistrationsResponse::getDate, dto -> dto));
        // Fill in monthCount and calculate totalUsers
        List<UserRegistrationsResponse> result = new ArrayList<>();
        // Calculate total users before fromDate to use as baseline
        long runningTotal = accountRepository.countByRoleRoleIdAndRegisterDateBefore(3L, fromDate);
        // Loop through each month from fromDate to toDate
        for (int i = 0; i < monthCount; i++) {
            LocalDate monthStart = fromDate.plusMonths(i).withDayOfMonth(1);
            UserRegistrationsResponse resp = map.getOrDefault(
                    monthStart,
                    new UserRegistrationsResponse(monthStart, 0L, 0L));
            // Update running total
            runningTotal += resp.getNewUsers();
            resp.setTotalUsers(runningTotal);
            result.add(resp);
        }
        return result;
    }

    /**
     * Sets log and sends notification for account actions.
     */
    private void setLogAndNotification(Long accountId, String action, String description,
                                       String title, String content) {
        // Check user applied
        Account currentAccount = getAccountOrThrow(accountId);
        // Get the current logged-in user
        String loginUserId = SecurityUtils.getCurrentUsername();
        if (loginUserId == null || loginUserId.isEmpty()) {
            throw new AppException("User not logged in!");
        }
        // Check logged-in user
        Account editorAccount = getAccountOrThrow(Long.valueOf(loginUserId));
        // Set the updater email
        currentAccount.setUpdateBy(editorAccount.getEmail());
        // Set role of the user applied
        String entityType = "";
        if (currentAccount.getRole().getRoleId() == 2) {
            entityType = "NHÂN VIÊN";
        } else {
            entityType = "THÀNH VIÊN";
        }
        // Log the activity
        activityLogService.log(
                editorAccount.getEmail(),
                action,
                entityType,
                currentAccount.getEmail(),
                description);
        // Notify admins
        List<Account> adminAccounts = accountRepository.findByRole_RoleName("ADMIN");
        if(adminAccounts.isEmpty()) {
            throw new AppException("No admin accounts found");
        }
        for (Account admin : adminAccounts) {
            if (!admin.getAccountId().equals(editorAccount.getAccountId())) {
                notificationService.notify(
                        admin,
                        title,
                        editorAccount.getFullName() + content + currentAccount.getEmail(),
                        "SYSTEM"
                );
            }
        }
    }

    /**
     * Find account by id, or throw AppException("Account not found").
     */
    public Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException("Account not found"));
    }

    /**
     * Check if email is already in use by another account
     * or throw AppException("Account already exists").
     */
    public void isAccountExist(String email) {
        boolean exists = accountRepository.existsByEmail(email);
        if (exists) {
            throw new AppException("Account already exists");
        }
    }

    /**
     * Check if phoneNumber is already in use by another account
     * or throw AppException("Phone already in use").
     */
    public void assertPhoneNotInUse(String phoneNumber, Long currentAccountId) {
        boolean exists;
        if (currentAccountId == null) {
            // Add new account: check if phone number exists
            exists = accountRepository.existsByPhoneNumber(phoneNumber);
        } else {
            // Update account: check if phone number exists but not for the current account
            exists = accountRepository.existsByPhoneNumberAndAccountIdNot(phoneNumber, currentAccountId);
        }
        if (exists) {
            throw new AppException("Phone already in use");
        }
    }

}
