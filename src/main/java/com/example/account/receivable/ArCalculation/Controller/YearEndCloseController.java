package com.example.account.receivable.ArCalculation.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.ArCalculation.Service.YearEndCloseService;
import com.example.account.receivable.Common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounting")
@RequiredArgsConstructor
public class YearEndCloseController {

    private final YearEndCloseService yearEndCloseService;

    @PostMapping("/close-year")
    public ResponseEntity<ApiResponse<String>> closeYear(
            @RequestParam Long companyId,
            @RequestParam int year
    ) {

        yearEndCloseService.closeYear(companyId, year);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Year closed successfully",
                        "OK"
                )
        );
    }
}
