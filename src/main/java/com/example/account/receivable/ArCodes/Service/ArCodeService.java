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
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.repository.UsersRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ArCodeService {
        private final UsersRepository usersRepository;
        private final ArCodeRepository arCodeRepository;


        public ArCodeResponseDto createArCode(
                        ArCodeCreateRequestDto dto,
                        Long userId
                ) {

                Users owner = usersRepository.findById(userId)
                        .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND, "User not found"));

                ArCode arCode = ArCode.builder()
                        .owner(owner)
                        .codeType(dto.getCodeType())
                        .code(dto.getCode())
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .isActive(true)
                        .isDeleted(false)
                        .build();

                ArCode saved = arCodeRepository.save(arCode);

                return toResponseDto(saved);
        }


        //Get Ar Codes of the Owner
        public List<ArCodeResponseDto> getAllActiveArCodes(Long userId) {
                return arCodeRepository
                        .findByOwnerIdAndIsDeletedFalse(userId)
                        .stream()
                        .map(this::toResponseDto)
                        .toList();
        }


        //Change Active to InActive
        public ArCodeResponseDto inactivateArCode(Long arCodeId, Long userId) {

                ArCode arCode = arCodeRepository
                        .findByIdAndOwnerIdAndIsDeletedFalse(arCodeId, userId)
                        .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND, "AR Code not found"));

                arCode.setActive(false);

                return toResponseDto(arCodeRepository.save(arCode));
        }

        //Change InActive to Active
        public ArCodeResponseDto activateArCode(Long arCodeId, Long userId) {

                ArCode arCode = arCodeRepository
                        .findByIdAndOwnerIdAndIsDeletedFalse(arCodeId, userId)
                        .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND, "AR Code not found"));

                arCode.setActive(true);

                return toResponseDto(arCodeRepository.save(arCode));
        }


        //Soft Delate 
        public void softDeleteArCode(Long arCodeId, Long userId) {

                ArCode arCode = arCodeRepository
                        .findByIdAndOwnerIdAndIsDeletedFalse(arCodeId, userId)
                        .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND, "AR Code not found"));

                arCode.setDeleted(true);
                arCode.setActive(false); // safety

                arCodeRepository.save(arCode);
        }



        //Update Ar Code
        public ArCodeResponseDto updateArCode(
                Long arCodeId,
                ArCodeUpdateRequestDto dto,
                Long userId
        ) {

                ArCode arCode = arCodeRepository
                        .findByIdAndOwnerIdAndIsDeletedFalse(arCodeId, userId)
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
