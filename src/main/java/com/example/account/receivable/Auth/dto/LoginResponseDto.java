package com.example.account.receivable.Auth.dto;

import com.example.account.receivable.User.entity.Users;

public class LoginResponseDto {
    private String token;
    private Users user;

    public LoginResponseDto(String token, Users user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public Users getUser() { return user; }
}

