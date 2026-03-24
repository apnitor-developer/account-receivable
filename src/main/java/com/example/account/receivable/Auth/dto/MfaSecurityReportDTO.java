package com.example.account.receivable.Auth.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MfaSecurityReportDTO {

    private String userName;
    private String email;
    private String roleName;
    private String mfaStatus;
    private Instant mfaEnabledAt;
}
