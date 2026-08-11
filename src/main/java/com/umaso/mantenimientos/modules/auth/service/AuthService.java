package com.umaso.mantenimientos.modules.auth.service;

import com.umaso.mantenimientos.modules.auth.dto.response.AuthResponse;
import com.umaso.mantenimientos.modules.auth.dto.request.ChangePasswordRequest;
import com.umaso.mantenimientos.modules.auth.dto.request.LoginRequest;
import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.modules.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User usuario = userRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        // Validar contraseña (BCrypt o texto plano temporal)
        boolean passwordValida = passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())
                || request.getContrasena().equals(usuario.getContrasena());

        if (!passwordValida) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        if (!usuario.getActivo()) {
            throw new RuntimeException("El usuario se encuentra inactivo");
        }

        // Token MOCK temporal
        String mockToken = "MOCK-TOKEN-" + UUID.randomUUID();

        return AuthResponse.builder()
                .token(mockToken)
                .usuario(usuario)
                .build();
    }

    @Transactional
    public void cambiarContrasena(ChangePasswordRequest request) {
        User usuario = userRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Encriptar nueva contraseña y cambiar la bandera
        usuario.setContrasena(passwordEncoder.encode(request.getNuevaContrasena()));
        usuario.setDebeCambiarContrasena(false);

        userRepository.save(usuario);
    }
}