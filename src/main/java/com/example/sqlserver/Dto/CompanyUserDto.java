package com.example.sqlserver.Dto;


import com.example.sqlserver.Entity.CompanyUser;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyUserDto {

    private Long id;
    private String name;
    private String email;
    private String status;
    private Long roleId;
    private String roleName;

    public static CompanyUserDto from(CompanyUser cu) {
        return CompanyUserDto.builder()
                .id(cu.getId())
                .name(cu.getName())
                .email(cu.getEmail())
                .status(cu.getStatus())
                .roleId(cu.getRole().getId())
                .roleName(cu.getRole().getName())
                .build();
    }
}

