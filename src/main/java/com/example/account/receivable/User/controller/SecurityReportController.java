package com.example.account.receivable.User.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.User.dto.RoleSecurityReportDto;
import com.example.account.receivable.User.service.SecurityReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/security-report")
@RequiredArgsConstructor
public class SecurityReportController {

    private final SecurityReportService securityReportService;

    // View Option A — Role → Objects → Permissions
    @GetMapping("/by-role")
    public ResponseEntity<ApiResponse<List<RoleSecurityReportDto>>> viewByRole(
            @RequestParam Long companyId
    ) {
        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Security report by role fetched successfully",
                        securityReportService.getReportByRole(companyId)
                )
        );
    }

    // View Option B — Object → Roles → Permissions
    @GetMapping("/by-object")
    public ResponseEntity<ApiResponse<Map<String, Map<String, Set<String>>>>> viewByObject(
            @RequestParam Long companyId
    ) {
        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Security report by object fetched successfully",
                        securityReportService.getReportByObject(companyId)
                )
        );
    }
}

