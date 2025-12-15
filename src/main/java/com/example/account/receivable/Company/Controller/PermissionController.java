package com.example.account.receivable.Company.Controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.Common.Premission.Permission;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getAllPermissions() {

        List<String> permissions = Arrays.stream(Permission.values())
                .map(Enum::name)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Permissions fetched successfully",
                        permissions
                )
        );
    }
}

