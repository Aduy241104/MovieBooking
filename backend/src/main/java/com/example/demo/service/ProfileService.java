package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.request.ChangePasswordRequest;
import com.example.demo.DTO.request.ProfileRequest;
import com.example.demo.DTO.response.AccountRespond;
import com.example.demo.DTO.response.ProfileDTO;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;
import com.example.demo.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    OtpService otpService;

    @Autowired
    SecurityUtils securityUtils;

    /**
     * Retrieves the user profile information based on the given account ID.
     *
     * @param id the ID of the account to retrieve the profile for
     * @return a {@link ProfileDTO} containing the user's personal profile data
     * @throws NotFoundException if no account is found with the provided ID
     */
    public ProfileDTO getProfile(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("account not found !"));
        ProfileDTO profileDTO = accountMapper.toPersonalProfile(account);
        return profileDTO;
    }

    /**
     * Updates the profile information of an account with the given ID using the
     * provided request data.
     *
     * @param id             the ID of the account to update
     * @param profileRequest the profile data to update (e.g., full name, gender,
     *                       phone number, date of birth)
     * @return an {@link AccountRespond} object containing the updated account
     *         information
     * @throws NotFoundException if the account with the specified ID is not found
     */
    public AccountRespond updateProfile(Long id, ProfileRequest profileRequest) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        account.setFullName(profileRequest.getFullName());
        account.setGender(profileRequest.getGender());
        account.setPhoneNumber(profileRequest.getPhoneNumber());
        account.setDateOfBirth(profileRequest.getDateOfBirth());

        accountRepository.save(account);
        AccountRespond accountRespond = accountMapper.toAccountRespond(account);
        return accountRespond;
    }

    /**
     * Changes the password for the specified account after verifying the old
     * password.
     *
     * @param accountId             the ID of the account to change the password for
     * @param changePasswordRequest contains the old and new passwords
     * @return a new JWT token after the password is successfully changed
     * @throws NotFoundException     if the account is not found
     * @throws UnauthorizedException if the old password is incorrect
     */
    public String changePassword(Long accountId, ChangePasswordRequest changePasswordRequest) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), account.getPassword())) {
            throw new UnauthorizedException("Old password is not correct");
        }
        account.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        accountRepository.save(account);
        String newToken = securityUtils.generateToken(account);
        return newToken;
    }

    /**
     * Sends an OTP to the provided email address to initiate the email change
     * process.
     *
     * @param email the new email address to be verified
     * @throws EmailAlreadyExistsException if the email is already in use by another
     *                                     account
     */
    public void requestChangeEmail(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("This Email already exists!");
        }
        otpService.sendOtp(email);
    }

    /**
     * Confirms the email change by verifying the provided OTP and updates the
     * account email.
     *
     * @param accountId the ID of the account to update
     * @param otp       the one-time password sent to the new email
     * @param newEmail  the new email address to be set
     * @return an {@link AccountRespond} containing the updated account information
     * @throws UnauthorizedException if the OTP or email is invalid
     * @throws NotFoundException     if the account is not found
     */
    public AccountRespond confirmChangeEmail(Long accountId, String otp, String newEmail) {
        boolean isValid = otpService.verifyOtp(newEmail, otp);

        if (!isValid) {
            throw new UnauthorizedException("Otp or email not correct");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found!"));

        account.setEmail(newEmail);
        otpService.clearOtp(newEmail);
        accountRepository.save(account);
        AccountRespond accountRespond = accountMapper.toAccountRespond(account);
        return accountRespond;
    }

    /**
     * Updates the avatar URL of the specified account.
     *
     * @param accountId the ID of the account to update
     * @param url       the new avatar image URL
     * @return an {@link AccountRespond} object containing the updated account
     *         information
     * @throws NotFoundException if the account with the given ID is not found
     */
    public AccountRespond changeAvatar(Long accountId, String url) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found!"));

        account.setAvatar(url);
        accountRepository.save(account);
        AccountRespond accountRespond = accountMapper.toAccountRespond(account);
        return accountRespond;
    }

}
