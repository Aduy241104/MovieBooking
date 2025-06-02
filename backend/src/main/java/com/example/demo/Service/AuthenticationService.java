package com.example.demo.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    OtpService otpService;

    @NonFinal
    protected static final String SIGNER_KEY = "tndQadt/oKJtCAnWMh2eq74jx2xdwHEbfKcOfeJaDDPLDayULMjWeoNGOfH9rfQA";

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

    // authentication cho table account
    public AuthRespond auth(AuthenticationRequest request) {
        Account account = accountRepository.findByEmailAndStatus(request.getUsername(), 1)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean checkPassword = request.getPassword().equals(account.getPassword());

        if (!checkPassword) {
            throw new UnauthorizedException("Invalid Password");
        } else {
            var token = generateToken(account);
            AccountRespond accountRespond = accountMapper.toAccountRespond(account);
            return AuthRespond.builder()
                    .authenticated(true)
                    .account(accountRespond)
                    .token(token)
                    .build();
        }
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
        Role defaultRole = roleRepository.findByRoleName("User")
                .orElseThrow(() -> new RuntimeException("Role Customer không tồn tại"));

        Account account = accountMapper.toAccount(registerRequest, defaultRole);
        accountRepository.save(account);
        otpService.clearOtp(accountWithOtp.getRegisterRequest().getEmail());
        return account;
    }

    // method to generate token
    private String generateToken(Account account) {

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(account.getEmail())
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
}
