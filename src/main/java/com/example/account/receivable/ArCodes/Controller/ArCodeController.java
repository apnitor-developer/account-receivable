package com.example.account.receivable.ArCodes.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.account.receivable.ArCodes.Dto.ArCodeCreateRequestDto;
import com.example.account.receivable.ArCodes.Dto.ArCodeResponseDto;
import com.example.account.receivable.ArCodes.Dto.ArCodeUpdateRequestDto;
import com.example.account.receivable.ArCodes.Service.ArCodeService;
import com.example.account.receivable.Common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/codes")
@RequiredArgsConstructor
public class ArCodeController {
    private final ArCodeService arCodeService;


    //Create Codes
    @PostMapping("/ar-codes/{companyId}")
    public ResponseEntity<ApiResponse<ArCodeResponseDto>> create(
        @PathVariable Long companyId,
        @Valid @RequestBody ArCodeCreateRequestDto dto
            
    ) {
        ArCodeResponseDto codes = arCodeService.createArCode(dto , companyId);

        ApiResponse<ArCodeResponseDto> body = ApiResponse.successResponse(201, "Codes created successfully", codes);
        return ResponseEntity.status(201).body(body);
    }



    //Get Ar Codes 
    @GetMapping("/ar-codes/{companyId}")
    public ResponseEntity<ApiResponse<List<ArCodeResponseDto>>> getAllActiveCodes(
            @PathVariable Long companyId
    ) {
        List<ArCodeResponseDto> codes = arCodeService.getAllActiveArCodes(companyId);

        ApiResponse<List<ArCodeResponseDto>> body =
                ApiResponse.successResponse(
                        200,
                        "AR Codes fetched successfully",
                        codes
                );

        return ResponseEntity.ok(body);
    }


    //Update Ar Code 
    @PutMapping("/ar-codes/{arCodeId}/{companyId}")
    public ResponseEntity<ApiResponse<ArCodeResponseDto>> update(
            @PathVariable Long arCodeId,
            @PathVariable Long companyId,
            @Valid @RequestBody ArCodeUpdateRequestDto dto
    ) {
        ArCodeResponseDto updated =
                arCodeService.updateArCode(arCodeId, dto, companyId);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "AR Code updated successfully",
                        updated
                )
        );
    }


    //Inactive code
    @PatchMapping("/ar-codes/{arCodeId}/inactive/{companyId}")
    public ResponseEntity<ApiResponse<ArCodeResponseDto>> inactivate(
            @PathVariable Long arCodeId,
            @PathVariable Long companyId
    ) {
        ArCodeResponseDto code =
                arCodeService.inactivateArCode(arCodeId, companyId);

        return ResponseEntity.ok(
                ApiResponse.successResponse(200, "AR Code inactivated", code)
        );
    }


    //Active Code
    @PatchMapping("/ar-codes/{arCodeId}/active/{companyId}")
    public ResponseEntity<ApiResponse<ArCodeResponseDto>> activate(
            @PathVariable Long arCodeId,
            @PathVariable Long companyId
    ) {
        ArCodeResponseDto code =
                arCodeService.activateArCode(arCodeId, companyId);

        return ResponseEntity.ok(
                ApiResponse.successResponse(200, "AR Code activated", code)
        );
    }



    //Soft Delate Ar Code
    @DeleteMapping("/ar-codes/{arCodeId}/{companyId}")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @PathVariable Long arCodeId,
            @PathVariable Long companyId
    ) {
        arCodeService.softDeleteArCode(arCodeId, companyId);

        return ResponseEntity.ok(
                ApiResponse.successResponse(200, "AR Code deleted successfully", null)
        );
    }
}


