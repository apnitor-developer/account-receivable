package com.example.account.receivable.Company.Controller;

import com.example.account.receivable.Company.Dto.ApiResponse;
import com.example.account.receivable.Company.Dto.RoleResponse;
import com.example.account.receivable.Company.Service.RoleService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*") 
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {

        List<RoleResponse> roles = roleService.getAllRoles();

        ApiResponse<List<RoleResponse>> body = ApiResponse.<List<RoleResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Roles fetched successfully")
                .data(roles)
                .build();

        return ResponseEntity.ok(body);
    }
}
