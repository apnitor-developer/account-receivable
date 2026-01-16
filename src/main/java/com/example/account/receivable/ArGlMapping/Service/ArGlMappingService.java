package com.example.account.receivable.ArGlMapping.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.ArCodes.Entity.ArCode;
import com.example.account.receivable.ArCodes.Repository.ArCodeRepository;
import com.example.account.receivable.ArGlMapping.Dto.ArGlMappingDto;
import com.example.account.receivable.ArGlMapping.Dto.ArGlMappingUpdate;
import com.example.account.receivable.ArGlMapping.Entity.ArGlMapping;
import com.example.account.receivable.ArGlMapping.Repository.ArGlMappingRepository;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.GLCodes.Entity.GlCode;
import com.example.account.receivable.GLCodes.Repository.GlCodeRepository;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArGlMappingService {

    private final ArGlMappingRepository mappingRepo;
    private final ArCodeRepository arCodeRepo;
    private final GlCodeRepository glCodeRepo;
    private final CompanyRepository companyRepo;
    private final UsersRepository userRepo;

    public ArGlMapping createMapping(
        Long companyId,
        Long userId,
        ArGlMappingDto dto
    ) {
        Company company = companyRepo.findById(companyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        Users user = userRepo.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ArCode arCode = arCodeRepo.findById(dto.getArCodeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AR code not found"));

        if (mappingRepo.existsByArCode_IdAndIsActiveTrue(arCode.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Active mapping already exists for this AR Code");
        }

        LocalDate effectiveFrom = LocalDate.now();

        GlCode dr = glCodeRepo.findById(dto.getDebitGlCodeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Debit GL Code not found"));

        GlCode cr = glCodeRepo.findById(dto.getCreditGlCodeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Credit GL Code not found"));

        ArGlMapping mapping = ArGlMapping.builder()
            .company(company)
            .arCode(arCode)
            .debitGlCode(dr)
            .creditGlCode(cr)
            .effectiveFrom(effectiveFrom)
            .isActive(true)
            .createdBy(user)
            .build();

        return mappingRepo.save(mapping);
    }


    // Method to find ArGlMapping by arCodeId
    public List<ArGlMapping> getMappingsByArCodeId(Long arCodeId) {
        return mappingRepo.findByArCodeIdAndActiveMappings(arCodeId);
    }



    // Update Mapping with new record is created
    public ArGlMapping updateMapping(
        Long companyId,
        Long userId,
        Long arCodeId,
        ArGlMappingUpdate dto
    ) {
        // Find the existing active mapping for the given AR Code
        ArGlMapping existingMapping = mappingRepo.findActiveMappingByArCodeIdAndCompanyId(arCodeId, companyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active mapping not found for this AR Code"));

        // Set the 'effectiveTo' date on the existing mapping (i.e., closing the old mapping)
        existingMapping.setEffectiveTo(LocalDate.now()); // The old mapping becomes invalid today
        mappingRepo.save(existingMapping); // Save the closed mapping

        // Create a new mapping with the updated details
        Company company = companyRepo.findById(companyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        Users user = userRepo.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ArCode arCode = arCodeRepo.findById(arCodeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AR Code not found"));

        GlCode debitGlCode = glCodeRepo.findById(dto.getDebitGlCodeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Debit GL Code not found"));

        GlCode creditGlCode = glCodeRepo.findById(dto.getCreditGlCodeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Credit GL Code not found"));

        LocalDate effectiveFrom = LocalDate.now(); // The new mapping starts today

        // Create the new AR → GL mapping
        ArGlMapping newMapping = ArGlMapping.builder()
            .company(company)
            .arCode(arCode)
            .debitGlCode(debitGlCode)
            .creditGlCode(creditGlCode)
            .effectiveFrom(effectiveFrom)
            .isActive(true) // New mapping is active
            .createdBy(user)
            .build();

        return mappingRepo.save(newMapping); // Save the new mapping
    }
}

