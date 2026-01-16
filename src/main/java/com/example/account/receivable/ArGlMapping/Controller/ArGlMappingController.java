package com.example.account.receivable.ArGlMapping.Controller;

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

import com.example.account.receivable.ArGlMapping.Dto.ArGlMappingDto;
import com.example.account.receivable.ArGlMapping.Dto.ArGlMappingUpdate;
import com.example.account.receivable.ArGlMapping.Entity.ArGlMapping;
import com.example.account.receivable.ArGlMapping.Service.ArGlMappingService;
import com.example.account.receivable.Common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ar-gl-mappings")
@RequiredArgsConstructor
public class ArGlMappingController {

    private final ArGlMappingService mappingService;

    @PostMapping("/companies/{companyId}/user/{userId}")
    public ResponseEntity<ApiResponse<ArGlMapping>> createMapping(
        @PathVariable Long companyId,
        @PathVariable Long userId,
        @Valid @RequestBody ArGlMappingDto dto
    ) {
        ArGlMapping mapping =
            mappingService.createMapping(companyId, userId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.successResponse(
                201,
                "AR → GL Mapping created successfully",
                mapping
            )
        );
    }


    //Get mapping with the Ar code
    @GetMapping("/ar-code/{arCodeId}")
    public ResponseEntity<ApiResponse<List<ArGlMapping>>> getMappingsByArCodeId(
        @PathVariable Long arCodeId
    ) {
        List<ArGlMapping> mappings = mappingService.getMappingsByArCodeId(arCodeId);
        return ResponseEntity.ok(
            ApiResponse.successResponse(
                200,
                "AR → GL Mappings fetched successfully",
                mappings
            )
        );
    }


    // Update the mapping
    @PutMapping("/companies/{companyId}/user/{userId}/ar-code/{arCodeId}")
    public ResponseEntity<ApiResponse<ArGlMapping>> updateMapping(
        @PathVariable Long companyId,
        @PathVariable Long userId,
        @PathVariable Long arCodeId,
        @Valid @RequestBody ArGlMappingUpdate dto
    ) {
        ArGlMapping updatedMapping = mappingService.updateMapping(companyId, userId, arCodeId, dto);

        return ResponseEntity.status(HttpStatus.OK).body(
            ApiResponse.successResponse(
                200,
                "AR → GL Mapping updated successfully",
                updatedMapping
            )
        );
    }
}

