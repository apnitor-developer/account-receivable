package com.example.account.receivable.ERA.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.ERA.Service.EraService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/era")
@RequiredArgsConstructor
public class EraController {

    private final EraService eraService;

    @PostMapping("/company/{companyId}/upload")
    public ResponseEntity<ApiResponse<String>> uploadEra(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file) {

        eraService.processEraFile(file);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "ERA file imported successfully",
                        "SUCCESS"
                )
        );
    }
}
