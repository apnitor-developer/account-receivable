package com.example.account.receivable.Auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Auth.dto.LoginDto;
import com.example.account.receivable.Auth.service.LoginService;
import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.User.entity.Users;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class LoginController {
    private final LoginService loginService;

    public LoginController(LoginService loginService){
        this.loginService = loginService;
    }
    

    @PostMapping()
    public ResponseEntity<ApiResponse<Users>> login(@Valid @RequestBody LoginDto dto) {
        Users user = loginService.login(dto);

        ApiResponse<Users> body = ApiResponse.successResponse(
            201, 
            "Login Successfully", 
            user
        );
        return ResponseEntity.status(200).body(body);
    }
}
