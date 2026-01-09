package com.example.account.receivable.ArCodes.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArCodeUpdateRequestDto {

    private String codeType;

    private String name;

    private String description;

    private String code;
}