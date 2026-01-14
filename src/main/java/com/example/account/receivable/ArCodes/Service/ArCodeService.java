package com.example.account.receivable.ArCodes.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.ArCodes.ArCodeType;
import com.example.account.receivable.ArCodes.Dto.ArCodeCreateRequestDto;
import com.example.account.receivable.ArCodes.Dto.ArCodeResponseDto;
import com.example.account.receivable.ArCodes.Dto.ArCodeUpdateRequestDto;
import com.example.account.receivable.ArCodes.Entity.ArCode;
import com.example.account.receivable.ArCodes.Repository.ArCodeRepository;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.repository.UsersRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ArCodeService {
        private final CompanyRepository companyRepository;
        private final ArCodeRepository arCodeRepository;


        public ArCodeResponseDto createArCode(
                        ArCodeCreateRequestDto dto,
                        Long companyId) {
                Company company = companyRepository.findById(companyId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Company not found"));

                ArCode arCode = ArCode.builder()
                                .company(company)
                                .codeType(dto.getCodeType())
                                .code(dto.getCode())
                                .name(dto.getName())
                                .description(dto.getDescription())
                                .isActive(true)
                                .isDeleted(false)
                                .build();

                return toResponseDto(arCodeRepository.save(arCode));
        }


        //Get Ar Codes of the company
        public List<ArCodeResponseDto> getAllActiveArCodes(Long companyId) {
                return arCodeRepository
                                .findByCompanyIdAndIsDeletedFalse(companyId)
                                .stream()
                                .map(this::toResponseDto)
                                .toList();
        }


        //Change Active to InActive
        public ArCodeResponseDto inactivateArCode(Long arCodeId, Long companyId) {

                ArCode arCode = arCodeRepository
                        .findByIdAndCompanyIdAndIsDeletedFalse(arCodeId, companyId)
                        .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND, "AR Code not found"));

                arCode.setActive(false);

                return toResponseDto(arCodeRepository.save(arCode));
        }

        //Change InActive to Active
        public ArCodeResponseDto activateArCode(Long arCodeId, Long companyId) {

                ArCode arCode = arCodeRepository
                        .findByIdAndCompanyIdAndIsDeletedFalse(arCodeId, companyId)
                        .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND, "AR Code not found"));

                arCode.setActive(true);

                return toResponseDto(arCodeRepository.save(arCode));
        }


        //Soft Delate 
        public void softDeleteArCode(Long arCodeId, Long companyId) {

                ArCode arCode = arCodeRepository
                        .findByIdAndCompanyIdAndIsDeletedFalse(arCodeId, companyId)
                        .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND, "AR Code not found"));

                arCode.setDeleted(true);
                arCode.setActive(false); // safety

                arCodeRepository.save(arCode);
        }



        //Update Ar Code
        public ArCodeResponseDto updateArCode(
                Long arCodeId,
                ArCodeUpdateRequestDto dto,
                Long companyId
        ) {

                ArCode arCode = arCodeRepository
                        .findByIdAndCompanyIdAndIsDeletedFalse(arCodeId, companyId)
                        .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND, "AR Code not found"));



                if (dto.getCodeType() != null) {
                        try {
                                ArCodeType type = ArCodeType.valueOf(dto.getCodeType().toUpperCase());
                                arCode.setCodeType(type);
                        } catch (IllegalArgumentException ex) {
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Invalid codeType: " + dto.getCodeType());
                        }
                }

                if (dto.getName() != null && !dto.getName().isBlank()) {
                        arCode.setName(dto.getName());
                }

                if (dto.getDescription() != null) {
                        arCode.setDescription(dto.getDescription());
                }

                if (dto.getCode() != null) {
                        arCode.setCode((dto.getCode()));
                }

                ArCode updated = arCodeRepository.save(arCode);

                return toResponseDto(updated);
        }


        // ✅ Mapping method inside service
        private ArCodeResponseDto toResponseDto(ArCode entity) {
                return ArCodeResponseDto.builder()
                        .id(entity.getId())
                        .codeType(entity.getCodeType())
                        .code(entity.getCode())
                        .name(entity.getName())
                        .description(entity.getDescription())
                        .isActive(entity.isActive())
                        .createdAt(entity.getCreatedAt())
                        .updatedAt(entity.getUpdatedAt())
                        .build();
        }
}
