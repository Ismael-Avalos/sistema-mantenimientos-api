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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(CreateUserRequest request) {

        String email = request.correo().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByCorreoIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }

        Role role = roleRepository.findById(request.rolId())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado."));

        User user = User.builder()
                .nombre(request.nombre())
                .correo(email)
                .contrasena(passwordEncoder.encode(request.contrasena()))
                .rol(role)
                .activo(true)
                .debeCambiarContrasena(true)
                .build();

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    // Método auxiliar para no repetir código al mapear en otros endpoints del CRUD
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNombre(),
                user.getCorreo(),
                user.getRol().getNombre(), // Asegúrate de que la entidad Role tenga getNombre()
                user.getActivo(),
                user.getDebeCambiarContrasena(), // <-- Campo añadido
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}
