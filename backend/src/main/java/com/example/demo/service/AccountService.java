package com.example.demo.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.DTO.response.ResPagination;
import com.example.demo.DTO.response.dashboard.UserRegistrationsResponse;
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


    public List<Account> getAllAccount() {
        List<Account> accounts = accountRepository.findAll();
        return accounts;
    }

    /**
     * Fetch all accounts with pagination and specification.
     *
     * @param spec     the specification to filter accounts
     * @param pageable the pagination information
     * @return a paginated response containing accounts
     */
    public ResPagination fetchAllAccountPagination(Specification<Account> spec, Pageable pageable) {

        Specification<Account> finalSpec = Specification.where(spec)
                .and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("isDeleted"), false));

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

    /**
     * Fetch an account by its ID.
     *
     * @param id the ID of the account
     * @return the account if found
     * @throws RuntimeException if the account is not found
     */
    public Account fetchAccountById(Long id) {
        Account currentAccount = accountRepository.findById(id).orElse(null);
        if(currentAccount == null) {
            throw new RuntimeException("Account not found");
        }
        return currentAccount;
    }


    /**
     * Create a new account.
     *
     * @param account the account to create
     * @return the created account
     * @throws RuntimeException if the role is not found or phone number is already in use
     */
    public Account handleCreateAccount(Account account) {
        Role role = roleRepository.findById(account.getRole().getRoleId()).orElse(null);
        if (role == null) {
            throw new RuntimeException("Role not found");
        }
        Account currentPhoneAccount = accountRepository.findByPhoneNumber(account.getPhoneNumber());
        if (currentPhoneAccount != null && !(currentPhoneAccount.getAccountId().equals(account.getAccountId()))) {
            throw new RuntimeException("Phone already in use");
        }
        account.setRole(role);
        return accountRepository.save(account);
    }

    /**
     * Update an existing account.
     *
     * @param account the account to update
     * @return the updated account
     * @throws RuntimeException if the account is not found or phone number is already in use
     */
    public Account handleUpdateAccountQuick(Account account) {
        Account currentAccount = accountRepository.findById(account.getAccountId()).orElse(null);
        if (currentAccount == null) {
            throw new RuntimeException("Account not found");
        }
        Account currentPhoneAccount = accountRepository.findByPhoneNumber(account.getPhoneNumber());
        if (currentPhoneAccount != null && !(currentPhoneAccount.getAccountId().equals(account.getAccountId()))) {
            throw new RuntimeException("Phone already in use");
        }

        currentAccount.setFullName(account.getFullName());
        currentAccount.setGender(account.getGender());
        currentAccount.setDateOfBirth(account.getDateOfBirth());
        currentAccount.setPhoneNumber(account.getPhoneNumber());

        // Lấy thông tin người dùng hiện tại từ SecurityUtils
        String loginUserId = SecurityUtils.getCurrentUsername();
        if(loginUserId == null || loginUserId.isEmpty()) {
            throw new RuntimeException("Current user not found");
        }
        Account user = accountRepository.findById(Long.valueOf(loginUserId))
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Khi người dùng cập nhật thông tin, lưu thông tin người dùng đã cập nhật
        currentAccount.setUpdateBy(user.getEmail());

        // Lưu log hoạt động cập nhật thông tin tài khoản
        String entityType = "";
        if(currentAccount.getRole().getRoleId() == 2){
            entityType = "NHÂN VIÊN";
        } else {
            entityType = "THÀNH VIÊN";
        }

        activityLogService.log(
                user.getEmail(),
                "CẬP NHẬT",
                entityType,
                currentAccount.getEmail(),
                "Cập nhật thông tin tài khoản   : " + currentAccount.getEmail()
        );

        return accountRepository.save(currentAccount);
    }

    /**
     * Update account information.
     *
     * @param id      the ID of the account to update
     * @param account the account information to update
     * @return the updated account
     * @throws RuntimeException if the account is not found or phone number is already in use
     */
    public Account handleUpdateAccountInfo(Long id, Account account) {
        Account currentAccount = accountRepository.findById(id).orElse(null);
        if (currentAccount == null) {
            throw new RuntimeException("Account not found");
        }
        Account currentPhoneAccount = accountRepository.findByPhoneNumber(account.getPhoneNumber());
        if (currentPhoneAccount != null && !(currentPhoneAccount.getAccountId().equals(account.getAccountId()))) {
            throw new RuntimeException("Phone already in use");
        }
        currentAccount.setFullName(account.getFullName());
        currentAccount.setGender(account.getGender());
        currentAccount.setDateOfBirth(account.getDateOfBirth());
        currentAccount.setPhoneNumber(account.getPhoneNumber());
        currentAccount.setIdentityCard(account.getIdentityCard());
        currentAccount.setScore(account.getScore());

        return accountRepository.save(currentAccount);
    }

    /**
     * Update the status of an account.
     *
     * @param account the account with updated status
     * @return the updated account
     * @throws RuntimeException if the account is not found
     */
    public Account handleUpdateStatusAccount(Account account) {
        Account currentAccount = accountRepository.findById(account.getAccountId()).orElse(null);
        if (currentAccount == null) {
            throw new RuntimeException("Account not found");
        }
        currentAccount.setStatus(account.getStatus());

        return accountRepository.save(currentAccount);
    }

    /**
     * Delete an account by marking it as deleted.
     *
     * @param account the account to delete
     * @return the updated account with isDeleted set to true
     * @throws RuntimeException if the account is not found
     */
    public Account handleDeleteAccount(@RequestBody Account account) {
        Account currentAccount = accountRepository.findById(account.getAccountId()).orElse(null);
        if (currentAccount == null) {
            throw new RuntimeException("Account not found");
        }
        currentAccount.setIsDeleted(true);
        return accountRepository.save(currentAccount);
    }

    /**
     * Find an account by email.
     *
     * @param email the email to search for
     * @return the account if found, null otherwise
     */
    public Account findAccountByEmail(String email) {
        return accountRepository.findByEmail(email).orElse(null);
    }

    /**
     * Handle the upload of an avatar for an account.
     *
     * @param id         the ID of the account
     * @param avatarFile the file to upload as avatar
     * @return the updated account with the new avatar
     * @throws RuntimeException if the account is not found or no file is uploaded
     */
    public Account handleUploadAvatar(Long id, MultipartFile avatarFile) {
        Account currentAccount = accountRepository.findById(id).orElse(null);
        if (currentAccount == null) {
            throw new RuntimeException("Account not found");
        }
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new RuntimeException("No file uploaded");
        }

        try {
            // Đường dẫn lưu file, ex: src/main/resources/static/avatars/
            String uploadDir = "uploads/avatars/";
            // Tạo thư mục nếu chưa tồn tại
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = "avatar_" + id + "_" + System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, avatarFile.getBytes());

            currentAccount.setAvatar(fileName);

            return accountRepository.save(currentAccount);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
    }

    /**
     * Get the total number of accounts by role.
     *
     * @param role the role to filter accounts
     * @return the total number of accounts with the specified role
     */
    public long getTotalAccountByRole(Role role) {
        return accountRepository.countByRole(role);
    }

    /**
     * Get user registrations statistics for the last 'monthCount' months.
     *
     * @param monthCount the number of months to look back
     * @return a DTO containing user registration statistics
     */
    public UserRegistrationsResponse getUserRegistrationsDTO(int monthCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusMonths(monthCount - 1).withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime toDate = now.plusDays(1).toLocalDate().atStartOfDay();

        List<UserRegistrationsResponse.UserRegistrations> userRegistrationsList = new ArrayList<>();
        List<Object[]> stats = accountRepository.getUserRegistrationsByMonth(fromDate, toDate);
        for (Object[] row : stats) {
            Object dateObj = row[0];
            LocalDate monthStart = switch (dateObj) {
                case java.sql.Timestamp ts -> ts.toLocalDateTime().toLocalDate();
                case java.time.Instant instant -> instant.atZone(ZoneId.systemDefault()).toLocalDate();
                case LocalDateTime ldt -> ldt.toLocalDate();
                case LocalDate ld -> ld;
                default -> throw new RuntimeException("Unknown date type: " + dateObj.getClass());
            };
            Long newUsers = ((Number) row[1]).longValue();
            Long totalUsers = ((Number) row[2]).longValue();
            userRegistrationsList.add(new UserRegistrationsResponse.UserRegistrations(monthStart, newUsers, totalUsers));
        }
        return UserRegistrationsResponse.builder()
                .data(userRegistrationsList)
                .build();
    }

}
