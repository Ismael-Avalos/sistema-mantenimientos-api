package com.umaso.mantenimientos.modules.users.controller;

import com.umaso.mantenimientos.modules.users.dto.request.CreateUserRequest;
import com.umaso.mantenimientos.modules.users.dto.request.UpdateUserRequest;
import com.umaso.mantenimientos.modules.users.dto.request.UpdateUserStatusRequest;
import com.umaso.mantenimientos.modules.users.dto.request.ResetPasswordRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.http.HttpStatus;
import java.util.UUID;
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

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request,
                               JwtAuthenticationToken authentication) {
        return userService.update(id, request, UUID.fromString(authentication.getToken().getSubject()));
    }

    @PatchMapping("/{id}/estado")
    public UserResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest request,
                                     JwtAuthenticationToken authentication) {
        return userService.updateStatus(id, request.activo(), UUID.fromString(authentication.getToken().getSubject()));
    }

    @PostMapping("/{id}/restablecer-contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable UUID id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, JwtAuthenticationToken authentication) {
        userService.delete(id, UUID.fromString(authentication.getToken().getSubject()));
    }
}
