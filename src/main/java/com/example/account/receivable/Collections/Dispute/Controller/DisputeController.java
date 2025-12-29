package com.example.account.receivable.Collections.Dispute.Controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Collections.Dispute.DTO.DisputeCodeResponseDto;
import com.example.account.receivable.Collections.Dispute.DTO.DisputeDTORequest;
import com.example.account.receivable.Collections.Dispute.DTO.DisputeStatusUpdateRequest;
import com.example.account.receivable.Collections.Dispute.Entity.Dispute;
import com.example.account.receivable.Collections.Dispute.Service.DisputeService;
import com.example.account.receivable.Common.ApiResponse;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {
        private final DisputeService disputeService;


    // Create Dispute
    @PostMapping
    public ResponseEntity<ApiResponse<Dispute>> createDispute(
        @RequestBody() DisputeDTORequest dto
    ) {
        Dispute dispute = disputeService.createDispute(dto);

        ApiResponse<Dispute> body = ApiResponse.successResponse(201, "Dispute created successfully", dispute);
        return ResponseEntity.status(201).body(body);
    }


    // Get Dispute by id
    @GetMapping("/{disputeId}")
    public ResponseEntity<ApiResponse<Dispute>> getDisputeByDisputeId(
            @PathVariable Long disputeId
    ) {
        Dispute dispute = disputeService.getByDisputeId(disputeId);

        ApiResponse<Dispute> body = ApiResponse.successResponse(200, "Dispute retreived successfully", dispute);
        return ResponseEntity.status(200).body(body);
    }


    //Get Dispute codes
    @GetMapping("/codes")
    public ResponseEntity<ApiResponse<List<DisputeCodeResponseDto>>> getDisputeCodes() {

        List<DisputeCodeResponseDto> codes = disputeService.getDisputeCodes();

        ApiResponse<List<DisputeCodeResponseDto>> body =
                ApiResponse.successResponse(200, "Dispute codes fetched successfully", codes);

        return ResponseEntity.ok(body);
    }


    // Get Company Dispute List
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<Dispute>>> getCompnayDisputeList(
            @PathVariable Long companyId
    ) {
        List<Dispute> dispute = disputeService.getCompanyDisputeList(companyId);

        ApiResponse<List<Dispute>> body = ApiResponse.successResponse(200, "Copmany Disputes retreived successfully", dispute);
        return ResponseEntity.status(200).body(body);
    }


    //Update Dispute Status
    @PatchMapping("/{disputeId}/status")
    public ResponseEntity<ApiResponse<Dispute>> updateDisputeStatus(
            @PathVariable Long disputeId,
            @RequestBody DisputeStatusUpdateRequest dto
    ) {
        Dispute updated = disputeService.updateDisputeStatus(disputeId, dto);

        ApiResponse<Dispute> body =
                ApiResponse.successResponse(200, "Dispute status updated successfully", updated);

        return ResponseEntity.ok(body);
    }


}
