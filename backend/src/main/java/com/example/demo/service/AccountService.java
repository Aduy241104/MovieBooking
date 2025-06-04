package com.example.demo.service;

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

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    public AccountService(AccountRepository accountRepository, RoleRepository roleRepository) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
    }

    public List<Account> getAllAccount() {
        List<Account> accounts = this.accountRepository.findAll();
        return accounts;
    }

    public ResPagination fetchAllAccountPagination(Specification<Account> spec, Pageable pageable) {
        Page<Account> accountPage = this.accountRepository.findAll(spec, pageable);

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

    public Account handleCreateAccount(Account account) {
        Role role = this.roleRepository.findById(account.getRole().getRoleId()).orElse(null);
        if (role == null) {
            throw new RuntimeException("Role not found");
        }
        Account currentPhoneAccount = this.accountRepository.findByPhoneNumber(account.getPhoneNumber());
        if (currentPhoneAccount != null && !(currentPhoneAccount.getAccountId().equals(account.getAccountId()))) {
            throw new RuntimeException("Phone already in use");
        }
        account.setRole(role);
        return this.accountRepository.save(account);
    }

    public Account handleUpdateAccount(Account account) {
        Account currentAccount = this.accountRepository.findById(account.getAccountId()).orElse(null);
        if (currentAccount == null) {
            throw new RuntimeException("Account not found");
        }
        Account currentPhoneAccount = this.accountRepository.findByPhoneNumber(account.getPhoneNumber());
        if (currentPhoneAccount != null && !(currentPhoneAccount.getAccountId().equals(account.getAccountId()))) {
            throw new RuntimeException("Phone already in use");
        }
        currentAccount.setFullName(account.getFullName());
        currentAccount.setGender(account.getGender());
        currentAccount.setDateOfBirth(account.getDateOfBirth());
        currentAccount.setPhoneNumber(account.getPhoneNumber());

        return this.accountRepository.save(currentAccount);
    }

    public Account findAccountByEmail(String email) {
        return this.accountRepository.findByEmail(email).orElse(null);
    }

}
