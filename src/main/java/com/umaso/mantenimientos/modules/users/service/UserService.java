package com.umaso.mantenimientos.modules.users.service;

import com.umaso.mantenimientos.modules.roles.entity.Role;
import com.umaso.mantenimientos.modules.roles.repository.RoleRepository;
import com.umaso.mantenimientos.modules.users.dto.request.CreateUserRequest;
import com.umaso.mantenimientos.modules.users.dto.response.UserResponse;
import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse create(CreateUserRequest request) {

        if (userRepository.existsByCorreo(request.correo())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }

        Role role = roleRepository.findById(request.rolId())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado."));

        User user = User.builder()
                .nombre(request.nombre())
                .correo(request.correo())
                .contrasena(passwordEncoder.encode(request.contrasena()))
                .rol(role)
                .activo(true)
                .debeCambiarContrasena(true)
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getNombre(),
                savedUser.getCorreo(),
                savedUser.getRol().getNombre(),
                savedUser.getActivo(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt()
        );
    }
}