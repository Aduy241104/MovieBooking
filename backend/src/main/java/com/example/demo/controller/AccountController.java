package com.example.demo.controller;

import com.example.demo.DTO.response.ResPagination;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/admin")
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public ResponseEntity<ApiResponse<Account>> getAccount(@PathVariable Long id) {
        ApiResponse<Account> response = ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch a account")
                .result(accountService.fetchAccountById(id))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/total-customer")
    public ResponseEntity<ApiResponse<Long>> totalCustomerAccount() {
        ApiResponse<Long> response = ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Count customer account successfully")
                .result(accountService.getTotalAccountByRole("CUSTOMER"))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<Account>> createAccount(@RequestBody Account account) {
        String passwordEncoded = passwordEncoder.encode(account.getPassword());
        account.setPassword(passwordEncoded);

        ApiResponse<Account> response = ApiResponse.<Account>builder()
                .status(HttpStatus.CREATED.value())
                .message("Create account successfully")
                .result(accountService.handleCreateAccount(account))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/accounts")
    public ResponseEntity<ApiResponse<Account>> updateAccountQuick(@RequestBody Account account) {
        ApiResponse<Account> response = ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Update account successfully in list user")
                .result(accountService.handleUpdateAccountQuick(account))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<Account>> updateAccountInfo(@PathVariable Long id, @RequestBody Account account) {
        ApiResponse<Account> response = ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Update account successfully in detail user")
                .result(accountService.handleUpdateAccountInfo(id, account))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/accounts/status")
    public ResponseEntity<ApiResponse<Account>> updateAccountStatus(@RequestBody Account account) {
        ApiResponse<Account> response = ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Update status account successfully")
                .result(accountService.handleUpdateStatusAccount(account))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/accounts/is-deleted")
    public ResponseEntity<ApiResponse<Account>> deleteAccount(@RequestBody Account account) {
        ApiResponse<Account> response = ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Delete account successfully")
                .result(accountService.handleDeleteAccount(account))
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/accounts/{id}/avatar")
    public ResponseEntity<ApiResponse<Account>> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("avatar") MultipartFile avatarFile) {
        ApiResponse<Account> response = ApiResponse.<Account>builder()
                .status(HttpStatus.OK.value())
                .message("Upload avatar successfully")
                .result(accountService.handleUploadAvatar(id, avatarFile))
                .build();
        return ResponseEntity.ok(response);
    }

    // @GetMapping("/accounts/customers-registrations")
    // public List<UserRegistrationsResponse> getUserRegistrations() {
    // return accountService.getUserRegistrationsDTO(6);
    // }

}
