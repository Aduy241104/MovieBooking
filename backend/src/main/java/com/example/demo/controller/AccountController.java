package com.example.demo.controller;

import java.util.List;

import com.example.demo.DTO.response.ResPagination;
import com.example.demo.DTO.response.dashboard.UserRegistrationsResponse;
import com.example.demo.model.Role;
import com.example.demo.service.RoleService;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.service.AccountService;
import com.example.demo.model.Account;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequestMapping("/api/public")
public class AccountController {

    private final AccountService accountService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public AccountController(AccountService accountService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/acc")
    public ApiResponse<List<Account>> getMethodName() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("User info: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        List<Account> accounts = accountService.getAllAccount();
        return ApiResponse.<List<Account>>builder()
                .result(accounts)
                .build();
    }

    @GetMapping("/accounts")
    public ApiResponse<ResPagination> getAllAccount(
            @Filter Specification<Account> spec, Pageable pageable) {

        return ApiResponse.<ResPagination>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch all account")
                .result(accountService.fetchAllAccountPagination(spec, pageable))
                .build();
    }

    @GetMapping("/accounts/{id}")
    public ApiResponse<Account> getAccount(@PathVariable Long id) {
        return ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch a account")
                .result(accountService.fetchAccountById(id))
                .build();
    }

    @GetMapping("/accounts/total-customer")
    public ApiResponse<Long> totalCustomerAccount() {
        Role role = roleService.findRoleByName("CUSTOMER");
        if(role == null) {
            throw new RuntimeException("Role not found");
        }
        return ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Count customer account")
                .result(accountService.getTotalAccountByRole(role))
                .build();
    }

    @PostMapping("/accounts")
    public ApiResponse<Account> createAccount(@RequestBody Account account) {
        if (roleService.findRoleById(account.getRole().getRoleId()) == null) {
            throw new RuntimeException("Role not found");
        }
        if (accountService.findAccountByEmail(account.getEmail()) != null) {
            throw new RuntimeException("Account already exists");
        }

        String passwordEncoded = passwordEncoder.encode(account.getPassword());
        account.setPassword(passwordEncoded);

        return ApiResponse.<Account>builder()
                .status(HttpStatus.CREATED.value())
                .message("Create account")
                .result(accountService.handleCreateAccount(account))
                .build();
    }

    @PutMapping("/accounts")
    public ApiResponse<Account> updateAccountQuick(@RequestBody Account account) {
        return ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Update a account in list user")
                .result(accountService.handleUpdateAccountQuick(account))
                .build();
    }

    @PutMapping("/accounts/{id}")
    public ApiResponse<Account> updateAccountInfo(@PathVariable Long id, @RequestBody Account account) {
        return ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Update a account in list user")
                .result(accountService.handleUpdateAccountInfo(id, account))
                .build();
    }

    @PutMapping("/accounts/status")
    public ApiResponse<Account> updateAccountStatus(@RequestBody Account account) {
        return ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Update status account")
                .result(accountService.handleUpdateStatusAccount(account))
                .build();
    }

    @PutMapping("/accounts/is-deleted")
    public ApiResponse<Account> deleteAccount(@RequestBody Account account) {
        return ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Delete account")
                .result(accountService.handleDeleteAccount(account))
                .build();
    }

    @PutMapping("/accounts/{id}/avatar")
    public ApiResponse<Account> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("avatar") MultipartFile avatarFile) {
        return ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Upload avatar")
                .result(accountService.handleUploadAvatar(id, avatarFile))
                .build();
    }

    @GetMapping("/accounts/customers-registrations")
    public UserRegistrationsResponse getUserRegistrations() {
        return accountService.getUserRegistrationsDTO(6);
    }

}
