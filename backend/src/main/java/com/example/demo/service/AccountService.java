package com.example.demo.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.example.demo.DTO.response.ResPagination;
import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
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

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    public AccountService(AccountRepository accountRepository, RoleRepository roleRepository) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
    }

    public List<Account> getAllAccount() {
        List<Account> accounts = accountRepository.findAll();
        return accounts;
    }

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

    public Account fetchAccountById(Long id) {
        Account currentAccount = accountRepository.findById(id).orElse(null);
        if(currentAccount == null) {
            throw new RuntimeException("Account not found");
        }
        return currentAccount;
    }

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

        return accountRepository.save(currentAccount);
    }

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

    public Account handleUpdateStatusAccount(Account account) {
        Account currentAccount = accountRepository.findById(account.getAccountId()).orElse(null);
        if (currentAccount == null) {
            throw new RuntimeException("Account not found");
        }
        currentAccount.setStatus(account.getStatus());

        return accountRepository.save(currentAccount);
    }

    public Account handleDeleteAccount(@RequestBody Account account) {
        Account currentAccount = accountRepository.findById(account.getAccountId()).orElse(null);
        if (currentAccount == null) {
            throw new RuntimeException("Account not found");
        }
        currentAccount.setIsDeleted(true);
        return accountRepository.save(currentAccount);
    }

    public Account findAccountByEmail(String email) {
        return accountRepository.findByEmail(email).orElse(null);
    }

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

}
