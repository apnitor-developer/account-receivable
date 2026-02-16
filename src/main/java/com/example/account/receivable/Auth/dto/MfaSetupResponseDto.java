package com.example.account.receivable.Auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MfaSetupResponseDto {
    private final String secret;
    private final String qrCodeImage;
}
