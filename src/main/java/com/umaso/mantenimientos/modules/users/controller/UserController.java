package com.umaso.mantenimientos.modules.users.controller;

import com.umaso.mantenimientos.modules.users.dto.request.CreateUserRequest;
import com.umaso.mantenimientos.modules.users.dto.response.UserResponse;
import com.umaso.mantenimientos.modules.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintenances/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }
}