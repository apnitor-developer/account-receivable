package com.example.account.receivable.GLCodes.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.GLCodes.Dto.GlCodeDto;
import com.example.account.receivable.GLCodes.Entity.GlCode;
import com.example.account.receivable.GLCodes.Repository.GlCodeRepository;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GlCodeService {

    private final GlCodeRepository glCodeRepository;
    private final CompanyRepository companyRepository;
    private final UsersRepository usersRepository;

    // Create GL codes
    public GlCode createGlCode(Long companyId, Long userId , GlCodeDto dto) {

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));


        Users user = usersRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (glCodeRepository.existsByCompanyIdAndGlCode(companyId, dto.getGlCode())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GL Code already exists for this company");
        }

        GlCode glCode = GlCode.builder()
            .company(company)
            .createdBy(user)
            .glCode(dto.getGlCode())
            .description(dto.getDescription())
            .accountType(dto.getAccountType())
            .isActive(true)
            .build();

        return glCodeRepository.save(glCode);
    }


    //Get GL codes
    public List<GlCode> getGlCodes(Long companyId) {
        return glCodeRepository.findByCompanyId(companyId);
    }


    //Update GL codes
    public GlCode updateGlCode(Long companyId, Long glCodeId, GlCodeDto dto) {

        GlCode glCode = glCodeRepository
            .findByIdAndCompanyId(glCodeId, companyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "GL code not found for this company"));

        if (dto.getGlCode() != null) {
            glCode.setGlCode(dto.getGlCode());
        }


        // Only update accountType if it's not null in the DTO
        if (dto.getDescription() != null) {
            glCode.setDescription(dto.getDescription());
        }
        
        if (dto.getAccountType() != null) {
            glCode.setAccountType(dto.getAccountType());
        }

        if (dto.getIsActive() != null) {
            glCode.setActive(dto.getIsActive());
        }

        return glCodeRepository.save(glCode);
    }
}

