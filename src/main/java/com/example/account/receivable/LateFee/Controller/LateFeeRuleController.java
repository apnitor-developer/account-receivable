package com.example.account.receivable.LateFee.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.LateFee.Entity.LateFeeRule;
import com.example.account.receivable.LateFee.Service.LateFeeRuleService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/late-fee")
@RequiredArgsConstructor
public class LateFeeRuleController {

    private final LateFeeRuleService lateFeeRuleService;

    //Create Late Fee Rules
    @PostMapping("/{companyId}")
    public ResponseEntity<ApiResponse<LateFeeRule>> createRule(
            @PathVariable Long companyId,
            @RequestBody LateFeeRule rule) {

        LateFeeRule createdRule = lateFeeRuleService.createRule(companyId, rule);

        ApiResponse<LateFeeRule> response = ApiResponse.successResponse(
                201,
                "Late fee rule created successfully",
                createdRule
        );

        return ResponseEntity.status(201).body(response);
    }


    //Get Late Fee Rules
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<LateFeeRule>> getRule(
            @PathVariable Long companyId) {

        LateFeeRule rule = lateFeeRuleService.getRule(companyId);

        ApiResponse<LateFeeRule> response = ApiResponse.successResponse(
                200,
                "Late fee rule fetched successfully",
                rule
        );

        return ResponseEntity.ok(response);
    }



    @GetMapping
    public ResponseEntity<ApiResponse<List<LateFeeRule>>> getAllRules() {

        List<LateFeeRule> rules = lateFeeRuleService.getAllRules();

        ApiResponse<List<LateFeeRule>> response = ApiResponse.successResponse(
                200,
                "Late fee rules fetched successfully",
                rules
        );

        return ResponseEntity.ok(response);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LateFeeRule>> updateRule(
            @PathVariable Long id,
            @RequestBody LateFeeRule rule) {

        LateFeeRule updatedRule = lateFeeRuleService.updateRule(id, rule);

        ApiResponse<LateFeeRule> response = ApiResponse.successResponse(
                200,
                "Late fee rule updated successfully",
                updatedRule
        );

        return ResponseEntity.ok(response);
    }


    //Delate Late Fee Rule
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRule(@PathVariable Long id) {

        lateFeeRuleService.deleteRule(id);

        ApiResponse<String> response = ApiResponse.successResponse(
                200,
                "Late fee rule deleted successfully",
                "Deleted"
        );

        return ResponseEntity.ok(response);
    }
}