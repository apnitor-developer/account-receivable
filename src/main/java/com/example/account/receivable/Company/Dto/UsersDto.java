package com.example.account.receivable.Company.Dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Long roleIds;
    private String roleNames;

}

