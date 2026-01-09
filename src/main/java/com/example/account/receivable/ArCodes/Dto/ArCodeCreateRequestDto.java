package com.example.account.receivable.ArCodes.Dto;


import com.example.account.receivable.ArCodes.ArCodeType;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArCodeCreateRequestDto {
    
    @NonNull
    private ArCodeType codeType;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;
}
