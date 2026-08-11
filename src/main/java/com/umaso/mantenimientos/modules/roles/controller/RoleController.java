package com.umaso.mantenimientos.modules.roles.controller;

import com.umaso.mantenimientos.modules.roles.dto.response.RoleResponse;
import com.umaso.mantenimientos.modules.roles.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintenances/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleResponse> findAll() {
        return roleService.findAll();
    }
}