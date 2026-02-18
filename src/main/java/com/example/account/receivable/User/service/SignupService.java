package com.example.account.receivable.User.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Common.EmailService;
import com.example.account.receivable.User.Enum.UserStatus;
import com.example.account.receivable.User.dto.SignupVerifyDto;
import com.example.account.receivable.User.dto.UserCreateDto;
import com.example.account.receivable.User.entity.PendingUserSignup;
import com.example.account.receivable.User.entity.Role;
import com.example.account.receivable.User.entity.UserRole;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.repository.PendingUserSignupRepository;
import com.example.account.receivable.User.repository.RoleRepository;
import com.example.account.receivable.User.repository.UserRoleRepository;
import com.example.account.receivable.User.repository.UsersRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.Duration;


@Service
@RequiredArgsConstructor
public class SignupService {

    private final PendingUserSignupRepository pendingRepo;
    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 5;

    /* =========================
       STEP 1: START SIGNUP
       ========================= */
    @Transactional
    public void startSignup(UserCreateDto dto) {

        // 1️⃣ Check existing user
        if (usersRepository.findByEmailAndDeletedFalse(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already exists with this email"
            );
        }

        // 2️⃣ Check pending signup (NO lambda)
        Optional<PendingUserSignup> existingOpt =
                pendingRepo.findByEmail(dto.getEmail());

        if (existingOpt.isPresent()) {
            PendingUserSignup existing = existingOpt.get();

            if (existing.getExpiresAt().isAfter(Instant.now())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "OTP already sent. Please verify your email."
                );
            }

            // OTP expired → cleanup
            pendingRepo.delete(existing);
            pendingRepo.flush(); // 🔥 REQUIRED for SQL Server
        }

        // 3️⃣ Create new pending signup
        String otp = generateOtp();

        PendingUserSignup pending = PendingUserSignup.builder()
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .otp(otp)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(OTP_EXPIRY_MINUTES)))
                .build();

        pendingRepo.save(pending);

        // 4️⃣ Send email (outside DB logic)
        try {
            String body = """
                <p>Your OTP for signup is:</p>
                <h2>%s</h2>
                <p>Valid for %d minutes.</p>
            """.formatted(otp, OTP_EXPIRY_MINUTES);

            emailService.sendWithAttachment(
                    dto.getEmail(),
                    "Verify your email",
                    body,
                    null
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to send OTP email. Please try again."
            );
        }
    }

    /* =========================
       STEP 2: VERIFY OTP & CREATE USER
       ========================= */
    @Transactional
    public Users verifyOtpAndCreateUser(SignupVerifyDto dto) {

        PendingUserSignup pending = pendingRepo.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Signup not found"
                ));

        if (pending.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "OTP expired"
            );
        }

        if (!pending.getOtp().equals(dto.getOtp())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid OTP"
            );
        }

        Role role = roleRepository.findByName("Admin")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Admin role not found"
                ));

        Users user = new Users();
        user.setFirstName(pending.getFirstName());
        user.setLastName(pending.getLastName());
        user.setEmail(pending.getEmail());
        user.setPassword(pending.getPasswordHash());
        user.setPasswordChangedAt(Instant.now());
        user.setStatus(UserStatus.ACTIVE);

        Users savedUser = usersRepository.save(user);

        userRoleRepository.save(
                UserRole.builder()
                        .user(savedUser)
                        .role(role)
                        .build()
        );

        // cleanup
        pendingRepo.delete(pending);

        return savedUser;
    }

    private String generateOtp() {
        return String.valueOf(100000 + new SecureRandom().nextInt(900000));
    }
}

