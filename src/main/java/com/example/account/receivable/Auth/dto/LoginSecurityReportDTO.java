package com.example.account.receivable.Auth.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginSecurityReportDTO {
    private Long userId;
    private String email;
    private String name;
    private Instant loginAt;
    private String ipAddress;
    private String status;
}
