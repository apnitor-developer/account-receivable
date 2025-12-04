package com.example.sqlserver.Service;

import com.example.sqlserver.Dto.RoleResponse;
import com.example.sqlserver.Entity.Role;
import com.example.sqlserver.Repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll(Sort.by("id").ascending());
        return roles.stream()
                .map(RoleResponse::from)
                .toList();
    }
}

