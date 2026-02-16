package com.example.account.receivable.Auth.service;

import com.example.account.receivable.Auth.dto.LoginDto;
import com.example.account.receivable.Auth.dto.LoginResponseDto;
import com.example.account.receivable.Auth.dto.MfaLoginDto;
import com.example.account.receivable.User.entity.UserStatus;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UsersRepository usersRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MfaService mfaService;

    @Value("${app.mfa.token-expiration-ms:300000}")
    private long mfaTokenExpirationMs;

    public LoginResponseDto login(LoginDto dto) {
        String normalizedEmail = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null;
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        Users user = usersRepository
                .findByEmailAndDeletedFalse(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid password"
            );
        }
        if (user.isMfaEnabled()) {

            // 1️⃣ Generate restricted JWT
            String tempJwt = jwtService.generateToken(
                    user.getEmail(),
                    Map.of(
                            "userId", user.getId(),
                            "email", user.getEmail(),
                            "roles", List.of("MFA_PENDING"),
                            "mfaVerified", false
                    )
            );

            // 2️⃣ Generate MFA token
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("type", "mfa_pending");

            String mfaToken = jwtService.generateToken(
                    user.getEmail(),
                    claims,
                    mfaTokenExpirationMs
            );

            return new LoginResponseDto(
                    tempJwt,   // JWT token
                    user,
                    true,      // mfa required
                    mfaToken   // mfa token
            );
        }

        return buildAuthenticatedResponse(user);
    }

    public LoginResponseDto loginWithMfa(MfaLoginDto dto) {
        Claims claims = parseMfaClaims(dto.getMfaToken());
        String email = claims.getSubject();
        Long userId = extractUserId(claims);

        Users user = usersRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid MFA token");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }

        if (!user.isMfaEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MFA is not enabled");
        }

        mfaService.checkLoginRateLimit(user.getId());
        if (!mfaService.verifyActiveCode(user, dto.getCode())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid MFA code");
        }

        return buildAuthenticatedResponse(user);
    }

    private LoginResponseDto buildAuthenticatedResponse(Users user) {
        List<String> roles = user.getUserRoles()
                .stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        String token = jwtService.generateToken(
                user.getEmail(),
                Map.of(
                        "userId", user.getId(),
                        "email", user.getEmail(),
                        "roles", roles
                )
        );

        return new LoginResponseDto(token, user, false, null);
    }

    private Claims parseMfaClaims(String token) {
        try {
            Claims claims = jwtService.parseClaims(token);
            String type = claims.get("type", String.class);
            if (!"mfa_pending".equals(type)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid MFA token");
            }
            return claims;
        } catch (Exception ex) {
            if (ex instanceof ResponseStatusException responseStatusException) {
                throw responseStatusException;
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired MFA token");
        }
    }

    private Long extractUserId(Claims claims) {
        Object value = claims.get("userId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        Object altValue = claims.get("user_id");
        if (altValue instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
