package com.example.account.receivable.ArCodes.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArCodeUpdateRequestDto {

    @NotBlank
    private String name;

    private String description;

    private String code;
}