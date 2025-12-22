package com.example.account.receivable.User.service;

import com.example.account.receivable.Company.Dto.RoleDto;
import com.example.account.receivable.Company.Dto.RoleResponse;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.User.entity.Role;
import com.example.account.receivable.User.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;


    //Create Role

    public RoleResponse createRole(Long companyId, RoleDto request) {

        if (roleRepository.existsByNameAndCompanyId(request.getName(), companyId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Role already exists for this company"
            );
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Company not found"
                ));

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(request.getPermissions())
                .company(company) // 🏢 COMPANY ROLE
                .build();

        return map(roleRepository.save(role));
    }


    private RoleResponse map(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions()
        );
    }

    //Get All Company Roles 
    public List<RoleResponse> getAllRoles(Long companyId) {
        return roleRepository.findByCompanyIdOrCompanyIsNull(companyId)
                .stream()
                .map(this::map)
                .toList();
    }

    public RoleResponse getRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Role not found"));

        return map(role);
    }
}

