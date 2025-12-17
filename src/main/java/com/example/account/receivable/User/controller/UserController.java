package com.example.account.receivable.User.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.User.dto.UserCreateDto;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.service.UserService;

@RestController
@RequestMapping("/users")

public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Users>> createUser(
        @Valid @RequestBody UserCreateDto dto
        ) {
            Users user = userService.register(dto);
            ApiResponse<Users> response = ApiResponse.successResponse(201, "User Created Successfully", user);
            return ResponseEntity.ok(response);
    }
}