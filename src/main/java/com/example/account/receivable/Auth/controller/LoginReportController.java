package com.example.account.receivable.Auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Auth.dto.LoginSecurityReportDTO;
import com.example.account.receivable.Auth.dto.MfaSecurityReportDTO;
import com.example.account.receivable.Auth.service.LoginService;
import com.example.account.receivable.Common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class LoginReportController {
    private final LoginService loginService;


    //Get Login Security Report for the Company Users
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<LoginSecurityReportDTO>>> getReportByCompany(@PathVariable Long companyId) {
        List<LoginSecurityReportDTO> login = loginService.getLoginReportByCompany(companyId);
        ApiResponse<List<LoginSecurityReportDTO>> body = ApiResponse.successResponse(
                200,
                "Login Security Report Retreived Successfully",
                login
        );
        return ResponseEntity.ok(body);
    }


    //Get MFA Security Report for the Company Users
    @GetMapping("/mfa/company/{companyId}")
    public ResponseEntity<ApiResponse<List<MfaSecurityReportDTO>>> getMfaReport(@PathVariable Long companyId) {
        List<MfaSecurityReportDTO> mfa = loginService.getMfaReportByCompany(companyId);
        ApiResponse<List<MfaSecurityReportDTO>> body = ApiResponse.successResponse(
                200,
                "MFA Security Report Retreived Successfully",
                mfa
        );
        return ResponseEntity.ok(body);
    }
}
