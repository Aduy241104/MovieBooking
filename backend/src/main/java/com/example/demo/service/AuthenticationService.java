package com.example.demo.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.DTO.request.AccountWithOtp;
import com.example.demo.DTO.request.AuthenticationRequest;
import com.example.demo.DTO.request.RegisterRequest;
import com.example.demo.DTO.response.AccountRespond;
import com.example.demo.DTO.response.AuthRespond;
import com.example.demo.enums.RoleTypes;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.RoleNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.model.Account;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.utils.GoogleTokenVerifier;
import com.example.demo.utils.SecurityUtils;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationService {

    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    OtpService otpService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    GoogleTokenVerifier googleTokenVerifier;

    @Autowired
    SecurityUtils securityUtils;

    /**
     * Authenticates a user based on the provided login credentials.
     *
     * @param request an {@link AuthenticationRequest} containing the user's email
     *                (username) and password
     * @return an {@link AuthRespond} object containing authentication status,
     *         access token,
     *         refresh token, and account information
     * @throws NotFoundException     if no active account is found with the provided
     *                               email
     * @throws UnauthorizedException if the password does not match the stored
     *                               password
     */
    public AuthRespond login(AuthenticationRequest request) {
        Account account = accountRepository.findByEmailAndStatus(request.getUsername(), 1)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String raw = request.getPassword();
        boolean match = passwordEncoder.matches(raw, account.getPassword());

        if (!match) {
            throw new UnauthorizedException("Invalid Password.");
        }

        var accessToken = securityUtils.generateToken(account);
        RefreshToken refreshToken = refreshTokenService.createRefresToken(account);
        AccountRespond accountRespond = accountMapper.toAccountRespond(account);
        
        return AuthRespond.builder()
                .authenticated(true)
                .account(accountRespond)
                .token(accessToken)
                .refresToken(refreshToken.getToken())
                .build();
    }

    /**
     * Sends an OTP (One-Time Password) to the email provided in the registration
     * request.
     * This method is typically called before account creation to verify email
     * ownership.
     *
     * @param registerRequest the {@link RegisterRequest} containing the user's
     *                        email
     * @throws EmailAlreadyExistsException if an account with the given email
     *                                     already exists
     */
    public void handleSendOtp(RegisterRequest registerRequest) {
        if (accountRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        otpService.sendOtp(registerRequest.getEmail());
    }

    /**
     * Creates a new account after validating the provided OTP.
     * The method maps the registration request into an {@link Account} entity,
     * encodes the password, assigns the default "CUSTOMER" role, and saves the
     * account.
     *
     * @param accountWithOtp the {@link AccountWithOtp} object that contains
     *                       registration data and the OTP
     * @return the newly created {@link Account} entity
     * @throws BadRequestException   if the OTP is invalid or has expired
     * @throws RoleNotFoundException if the default CUSTOMER role cannot be found in
     *                               the system
     */
    public Account createAccount(AccountWithOtp accountWithOtp) {
        // Kiểm tra OTP
        boolean isOtpValid = otpService.verifyOtp(accountWithOtp.getRegisterRequest().getEmail(),
                accountWithOtp.getVerifyOtp());

        if (!isOtpValid) {
            throw new BadRequestException("OTP is invalid or expired");
        }

        RegisterRequest registerRequest = accountWithOtp.getRegisterRequest();

        Role defaultRole = roleRepository.findByRoleName(RoleTypes.CUSTOMER.name())
                .orElseThrow(() -> new RoleNotFoundException("Role Customer does not exist"));

        Account account = accountMapper.toAccount(registerRequest, defaultRole);
        account.setSocialAccountType("LOCAL");
        account.setPassword(passwordEncoder.encode(account.getPassword()));

        accountRepository.save(account);
        otpService.clearOtp(accountWithOtp.getRegisterRequest().getEmail());
        return account;
    }

    /**
     * Handles the "forgot password" process by verifying the email and sending a
     * reset password link or OTP.
     *
     * @param email the email address of the account requesting password reset
     * @throws NotFoundException if no account exists with the given email
     */
    public void handleForgotPassword(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Account does not exists"));
        otpService.sendForgotPasswordLink(account);
    }

    /**
     * Resets the user's password after validating the provided OTP.
     * This method updates the password for the account associated with the given
     * email.
     *
     * @param email   the email address of the account
     * @param otp     the one-time password used to verify the reset request
     * @param newPass the new password to be set
     * @throws NotFoundException   if the account is not found
     * @throws BadRequestException if the OTP is invalid or expired
     */
    @Transactional
    public void handleResetPassword(String email, String otp, String newPass) {
        boolean isOtpValid = otpService.verifyOtp(email, otp);

        if (!isOtpValid) {
            throw new BadRequestException("OTP is invalid or expired");
        }
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("account not found"));

        account.setPassword(passwordEncoder.encode(newPass));
        accountRepository.save(account);
        otpService.clearOtp(email);
    }

    /**
     * Handles user login via Google OAuth using the provided ID token.
     * <p>
     * If the account associated with the Google email already exists and is active,
     * it will be used for login. Otherwise, a new account will be created with
     * default
     * CUSTOMER role and marked as a Google social account.
     *
     * @param idToken the Google ID token received from the client after Google
     *                sign-in
     * @return an {@link AuthRespond} object containing authentication status,
     *         access token,
     *         refresh token, and account information
     * @throws NotFoundException     if the ID token is invalid or if the existing
     *                               account is not found
     * @throws RoleNotFoundException if the CUSTOMER role does not exist in the
     *                               database
     */
    public AuthRespond handleLoginWithGoogle(String idToken) {

        Map<String, Object> payload = googleTokenVerifier.verify(idToken);

        if (payload == null) {
            throw new NotFoundException("id not valid");
        }

        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        boolean checkExists = accountRepository.existsByEmail(email);
        Account account;

        if (checkExists) {
            account = accountRepository.findByEmailAndStatus(email, 1)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            // if (account.getSocialAccountType().equals("LOCAL")) {
            // throw new EmailAlreadyExistsException("Email này đã được đăng ký bằng email
            // và mật khẩu.");
            // }
        } else {
            Role defaultRole = roleRepository.findByRoleName(RoleTypes.CUSTOMER.name())
                    .orElseThrow(() -> new RoleNotFoundException("Role Customer does not exist"));
            account = Account.builder()
                    .email(email)
                    .fullName(name)
                    .avatar(picture)
                    .role(defaultRole)
                    .status(1)
                    .isDeleted(false)
                    .socialAccountType("GOOGLE")
                    .build();
            accountRepository.save(account);
        }

        var token = securityUtils.generateToken(account);
        RefreshToken refreshToken = refreshTokenService.createRefresToken(account);

        AccountRespond accountRespond = accountMapper.toAccountRespond(account);
        return AuthRespond.builder()
                .authenticated(true)
                .account(accountRespond)
                .token(token)
                .refresToken(refreshToken.getToken())
                .build();
    }
}
