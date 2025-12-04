package com.example.account.receivable.Company.Dto;


import lombok.*;

import java.time.OffsetDateTime;

import com.example.account.receivable.Company.Entity.CompanyOpeningBalanceFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpeningBalanceFileResponse {

    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private OffsetDateTime uploadedAt;

    public static OpeningBalanceFileResponse from(CompanyOpeningBalanceFile f) {
        return OpeningBalanceFileResponse.builder()
                .id(f.getId())
                .fileName(f.getFileName())
                .contentType(f.getContentType())
                .fileSize(f.getFileSize())
                .uploadedAt(f.getUploadedAt())
                .build();
    }
}

