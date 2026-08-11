package com.umaso.mantenimientos.modules.roles.service;

import com.umaso.mantenimientos.modules.roles.dto.response.RoleResponse;
import com.umaso.mantenimientos.modules.roles.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAll()
                .stream()
                .map(role -> new RoleResponse(role.getId(), role.getNombre(), role.getDescripcion()))
                .toList();
    }
}