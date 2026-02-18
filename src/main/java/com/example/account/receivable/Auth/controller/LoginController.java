package com.example.account.receivable.Auth.controller;

import com.example.account.receivable.Auth.dto.ChangePasswordDto;
import com.example.account.receivable.Auth.dto.LoginDto;
import com.example.account.receivable.Auth.dto.LoginResponseDto;
import com.example.account.receivable.Auth.dto.MfaCodeDto;
import com.example.account.receivable.Auth.dto.MfaLoginDto;
import com.example.account.receivable.Auth.service.LoginService;
import com.example.account.receivable.Auth.service.MfaService;
import com.example.account.receivable.Common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;
    private final MfaService mfaService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginDto dto) {
        LoginResponseDto user = loginService.login(dto);

        String message = Boolean.TRUE.equals(user.getMfaRequired())
                ? "OTP sent to email"
                : "Login Successfully";
        ApiResponse<LoginResponseDto> body = ApiResponse.successResponse(
                201,
                message,
                user
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordDto dto
    ) {
        String email = currentUserEmail();

        loginService.changePassword(email, dto);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Password changed successfully",
                        null
                )
        );
    }

    @PostMapping("/login/mfa")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginWithMfa(@Valid @RequestBody MfaLoginDto dto) {
        LoginResponseDto user = loginService.loginWithMfa(dto);
        ApiResponse<LoginResponseDto> body = ApiResponse.successResponse(
                200,
                "Login Successfully",
                user
        );
        return ResponseEntity.ok(body);
    }

    //Send OTP to Email
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<Void>> sendMfaEmailOtp() {
        String email = currentUserEmail();
        mfaService.sendEmailOtp(email);
        ApiResponse<Void> body = ApiResponse.successResponse(
                200,
                "Email OTP sent successfully",
                null
        );
        return ResponseEntity.ok(body);
    }

    // Verify email
    @PostMapping("/mfa/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyMfaEmailOtp(@Valid @RequestBody MfaCodeDto dto) {
        String email = currentUserEmail();
        mfaService.verifyEmailOtp(email, dto.getCode());
        ApiResponse<Void> body = ApiResponse.successResponse(
                200,
                "Email OTP verified successfully",
                null
        );
        return ResponseEntity.ok(body);
    }

    private String currentUserEmail() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? String.valueOf(auth.getPrincipal()) : null;
    }
}
