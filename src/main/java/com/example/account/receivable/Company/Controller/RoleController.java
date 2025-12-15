package com.example.account.receivable.Company.Controller;

import com.example.account.receivable.Company.Dto.RoleDto;
import com.example.account.receivable.Company.Dto.RoleResponse;
import com.example.account.receivable.Company.Service.RoleService;
import com.example.account.receivable.Common.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;


    // Create role
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @RequestBody RoleDto request
    ) {
        RoleResponse response = roleService.createRole(request);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        201,
                        "Role created successfully",
                        response
                )
        );
    }

    //get All roles
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {

        List<RoleResponse> roles = roleService.getAllRoles();

        ApiResponse<List<RoleResponse>> body =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Roles fetched successfully",
                        roles
                );

        return ResponseEntity.ok(body);
    }


    // Get role by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Role fetched successfully",
                        roleService.getRole(id)
                )
        );
    }
}
