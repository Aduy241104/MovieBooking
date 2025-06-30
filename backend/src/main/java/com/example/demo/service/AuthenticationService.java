package com.example.demo.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.DTO.request.AccountWithOtp;
import com.example.demo.DTO.request.AuthenticationRequest;
import com.example.demo.DTO.request.IntrospectRequest;
import com.example.demo.DTO.request.RegisterRequest;
import com.example.demo.DTO.response.AccountRespond;
import com.example.demo.DTO.response.AuthRespond;
import com.example.demo.DTO.response.IntrospectRespond;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.model.Account;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

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

    // authentication cho table account
    public AuthRespond auth(AuthenticationRequest request) {
        Account account = accountRepository.findByEmailAndStatus(request.getUsername(), 1)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String raw = request.getPassword();
        boolean match = passwordEncoder.matches(raw, account.getPassword());

        if (!match) {
            throw new UnauthorizedException("Invalid Password.");
        }
        var token = generateToken(account);
        RefreshToken refreshToken = refreshTokenService.createRefresToken(account);

        AccountRespond accountRespond = accountMapper.toAccountRespond(account);
        return AuthRespond.builder()
                .authenticated(true)
                .account(accountRespond)
                .token(token)
                .refresToken(refreshToken.getToken())
                .build();
    }

    // register phien ban moi nhat
    public void handleSendOtp(RegisterRequest registerRequest) {
        if (accountRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã tồn tại");
        }
        otpService.sendOtp(registerRequest.getEmail());
    }

    public Account createAccount(AccountWithOtp accountWithOtp) {
        // Kiểm tra OTP
        boolean isOtpValid = otpService.verifyOtp(accountWithOtp.getRegisterRequest().getEmail(),
                accountWithOtp.getVerifyOtp());

        if (!isOtpValid) {
            throw new UnauthorizedException("OTP không hợp lệ hoặc đã hết hạn.");
        }

        RegisterRequest registerRequest = accountWithOtp.getRegisterRequest();
        Role defaultRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role Customer không tồn tại"));

        Account account = accountMapper.toAccount(registerRequest, defaultRole);
        account.setPassword(passwordEncoder.encode(account.getPassword()));

        accountRepository.save(account);
        otpService.clearOtp(accountWithOtp.getRegisterRequest().getEmail());
        return account;
    }

    public void handleForgotPassword(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Account does not exists"));
        otpService.sendForgotPasswordLink(account);
    }

    @Transactional
    public void handleResetPassword(String email, String otp, String newPass) {
        boolean isOtpValid = otpService.verifyOtp(email, otp);

        if (!isOtpValid) {
            throw new UnauthorizedException("Invalid or incorrect OTP.");
        }
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("account not found"));

        account.setPassword(passwordEncoder.encode(newPass));
        accountRepository.save(account);
        otpService.clearOtp(email);
    }

    // method to generate token
    public String generateToken(Account account) {

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(account.getAccountId()))
                .issuer("anhduy.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .claim("scope", account.getRole().getRoleName())
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    // check valid token
    public IntrospectRespond introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean verified = signedJWT.verify(verifier);

        return IntrospectRespond.builder()
                .valid(verified && expiryTime.after(new Date()))
                .build();
    }
}
