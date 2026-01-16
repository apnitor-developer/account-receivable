package com.example.account.receivable.ArCodes.Dto;


import lombok.*;

import java.time.Instant;

import com.example.account.receivable.ArCodes.ArCodeType;
import com.example.account.receivable.ArCodes.GlMappingStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArCodeResponseDto {

    private Long id;

    private ArCodeType codeType;

    private String code;

    private String name;

    private String description;

    private boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;

    private GlMappingStatus glMappingStatus;
}
