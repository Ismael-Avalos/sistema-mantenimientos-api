package com.umaso.mantenimientos.modules.auth.controller;

import com.umaso.mantenimientos.modules.auth.dto.response.AuthResponse;
import com.umaso.mantenimientos.modules.auth.dto.request.ChangePasswordRequest;
import com.umaso.mantenimientos.modules.auth.dto.request.LoginRequest;
import com.umaso.mantenimientos.modules.auth.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/cambiar-contrasena")
    public ResponseEntity<?> cambiarContrasena(@RequestBody ChangePasswordRequest request) {
        try {
            authService.cambiarContrasena(request);
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}