package com.example.account.receivable.GLCodes.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.GLCodes.Dto.GlCodeDto;
import com.example.account.receivable.GLCodes.Entity.GlCode;
import com.example.account.receivable.GLCodes.Service.GlCodeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gl-codes")
@RequiredArgsConstructor
public class GlCodeController {

    private final GlCodeService glCodeService;

    // Create GL Codes
    @PostMapping("/company/{companyId}/user/{userId}")
    public ResponseEntity<ApiResponse<GlCode>> createGlCode(
        @PathVariable Long companyId,
        @PathVariable Long userId,
        @RequestBody @Valid GlCodeDto dto
    ) {
        GlCode glCode = glCodeService.createGlCode(companyId, userId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.successResponse(
                201,
                "GL Code created successfully",
                glCode
            )
        );
    }

    // Get GL Codes
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<GlCode>>> getGlCodes(
        @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(
            ApiResponse.successResponse(
                200,
                "GL Codes fetched successfully",
                glCodeService.getGlCodes(companyId)
            )
        );
    }

    // Update GL Codes
    @PutMapping("/company/{companyId}/{glCodeId}")
    public ResponseEntity<ApiResponse<GlCode>> updateGlCode(
        @PathVariable Long companyId,
        @PathVariable Long glCodeId,
        @RequestBody @Valid GlCodeDto dto
    ) {
        GlCode updated = glCodeService.updateGlCode(companyId, glCodeId, dto);

        return ResponseEntity.ok(
            ApiResponse.successResponse(
                200,
                "GL Code updated successfully",
                updated
            )
        );
    }
}

