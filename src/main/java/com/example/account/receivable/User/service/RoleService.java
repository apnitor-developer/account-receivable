package com.example.account.receivable.User.service;

import com.example.account.receivable.Common.Premission.Permission;
import com.example.account.receivable.Company.Dto.RoleDto;
import com.example.account.receivable.Company.Dto.RoleResponse;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.User.dto.UpdateRoleDto;
import com.example.account.receivable.User.entity.Role;
import com.example.account.receivable.User.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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



        //Get All Company Roles 
        public List<RoleResponse> getAllRoles(Long companyId) {
                return roleRepository.findByCompanyIdOrCompanyIsNull(companyId)
                        .stream()
                        .map(this::map)
                        .toList();
        }


        // Get Role
        public RoleResponse getRole(Long id) {
                Role role = roleRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Role not found"));

                return map(role);
        }



        // Update Role
        public RoleResponse updateRole(
                Long roleId,
                Long companyId,
                UpdateRoleDto request
        ) {

                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Role not found"));

                // ❌ Global roles cannot be updated
                if (role.getCompany() == null) {
                throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Global roles cannot be updated");
                }

                // ❌ Ensure role belongs to company
                if (!role.getCompany().getId().equals(companyId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role does not belong to this company");
                }

                // ✅ Update name if provided
                if (request.getName() != null) {
                role.setName(request.getName());
                }

                // ✅ Update description if provided
                if (request.getDescription() != null) {
                role.setDescription(request.getDescription());
                }

                Set<Permission> permissions = role.getPermissions();
                if (permissions == null) {
                permissions = new HashSet<>();
                }

                // ➕ Add permissions
                if (request.getAddPermissions() != null) {
                permissions.addAll(request.getAddPermissions());
                }

                // ➖ Remove permissions
                if (request.getRemovePermissions() != null) {
                permissions.removeAll(request.getRemovePermissions());
                }

                role.setPermissions(permissions);

                Role savedRole = roleRepository.save(role);

                return new RoleResponse(
                        savedRole.getId(),
                        savedRole.getName(),
                        savedRole.getDescription(),
                        savedRole.getPermissions()
                );
        }


        //Response Method
        private RoleResponse map(Role role) {
                return new RoleResponse(
                        role.getId(),
                        role.getName(),
                        role.getDescription(),
                        role.getPermissions()
                );
        }

}

