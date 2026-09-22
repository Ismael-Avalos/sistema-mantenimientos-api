package com.umaso.mantenimientos.modules.users.service;

import com.umaso.mantenimientos.modules.auth.service.RefreshTokenService;
import com.umaso.mantenimientos.modules.maintenances.repository.MaintenanceRepository;
import com.umaso.mantenimientos.modules.roles.entity.Role;
import com.umaso.mantenimientos.modules.roles.repository.RoleRepository;
import com.umaso.mantenimientos.modules.users.dto.request.*;
import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.modules.users.repository.UserRepository;
import com.umaso.mantenimientos.shared.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final RefreshTokenService sessions = mock(RefreshTokenService.class);
    private final MaintenanceRepository maintenances = mock(MaintenanceRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final UserService service = new UserService(users, roles, encoder, sessions, maintenances);
    private final UUID actor = UUID.randomUUID();
    private User user;

    @BeforeEach
    void setup() {
        Role role = Role.builder().id(UUID.randomUUID()).nombre("TECNICO").build();
        user = User.builder().id(UUID.randomUUID()).nombre("Usuario").correo("user@example.com")
                .rol(role).contrasena(encoder.encode("Original123")).debeCambiarContrasena(false).build();
        when(users.findByIdWithRole(user.getId())).thenReturn(Optional.of(user));
        when(users.save(any())).thenAnswer(call -> call.getArgument(0));
        when(roles.findById(role.getId())).thenReturn(Optional.of(role));
    }

    @Test
    void creationNormalizesEmailHashesPasswordAndRequiresChange() {
        service.create(new CreateUserRequest(" Nuevo ", "NUEVO@example.com", "Temporal123", user.getRol().getId()));
        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        User created = captor.getValue();
        assertThat(created.getCorreo()).isEqualTo("nuevo@example.com");
        assertThat(created.getNombre()).isEqualTo("Nuevo");
        assertThat(created.getActivo()).isTrue();
        assertThat(created.getDebeCambiarContrasena()).isTrue();
        assertThat(encoder.matches("Temporal123", created.getContrasena())).isTrue();
    }

    @Test
    void resetHashesTemporaryPasswordRequiresChangeAndRevokesSessionsWithoutActivatingAccount() {
        user.setActivo(false);
        service.resetPassword(user.getId(), new ResetPasswordRequest("Temporal123"));
        assertThat(encoder.matches("Temporal123", user.getContrasena())).isTrue();
        assertThat(user.getDebeCambiarContrasena()).isTrue();
        assertThat(user.getActivo()).isFalse();
        assertThat(user.getSecurityVersion()).isEqualTo(1);
        verify(sessions).revokeAll(user.getId());
    }

    @Test
    void resetRejectsReusingCurrentPassword() {
        assertThatThrownBy(() -> service.resetPassword(user.getId(), new ResetPasswordRequest("Original123")))
                .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getCode()).isEqualTo("AUTH_PASSWORD_REUSED"));
        verifyNoInteractions(sessions);
    }

    @Test
    void roleChangeInvalidatesExistingAuthorities() {
        Role admin = Role.builder().id(UUID.randomUUID()).nombre("ADMIN").build();
        when(roles.findById(admin.getId())).thenReturn(Optional.of(admin));
        service.update(user.getId(), new UpdateUserRequest("Editado", user.getCorreo(), admin.getId(), true), actor);
        assertThat(user.getRol()).isEqualTo(admin);
        assertThat(user.getSecurityVersion()).isEqualTo(1);
        verify(sessions).revokeAll(user.getId());
    }

    @Test
    void deactivationRevokesSessionsAndReactivationDoesNotRestoreOldTokens() {
        service.updateStatus(user.getId(), false, actor);
        assertThat(user.getActivo()).isFalse();
        service.updateStatus(user.getId(), true, actor);
        assertThat(user.getActivo()).isTrue();
        assertThat(user.getSecurityVersion()).isEqualTo(2);
        verify(sessions, times(2)).revokeAll(user.getId());
    }

    @Test
    void duplicateEmailCannotBeAssignedToAnotherAccount() {
        when(users.findByCorreoIgnoreCase("other@example.com"))
                .thenReturn(Optional.of(User.builder().id(UUID.randomUUID()).build()));
        assertThatThrownBy(() -> service.update(user.getId(), new UpdateUserRequest(
                "Editado", "OTHER@example.com", user.getRol().getId(), true), actor))
                .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getCode()).isEqualTo("USER_EMAIL_EXISTS"));
        verify(users, never()).save(any());
    }

    @Test
    void cannotDeleteDeactivateOrChangeOwnRole() {
        assertThatThrownBy(() -> service.delete(user.getId(), user.getId())).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> service.updateStatus(user.getId(), false, user.getId())).isInstanceOf(ApiException.class);
        Role role = Role.builder().id(UUID.randomUUID()).nombre("ADMIN").build();
        when(roles.findById(role.getId())).thenReturn(Optional.of(role));
        assertThatThrownBy(() -> service.update(user.getId(), new UpdateUserRequest(
                "Editado", user.getCorreo(), role.getId(), true), user.getId())).isInstanceOf(ApiException.class);
        verify(users, never()).delete(any());
        verify(users, never()).save(any());
    }

    @Test
    void historyPreventsDeletion() {
        when(maintenances.existsByResponsableId(user.getId())).thenReturn(true);
        assertThatThrownBy(() -> service.delete(user.getId(), actor))
                .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getCode()).isEqualTo("USER_HAS_MAINTENANCES"));
        verify(users, never()).delete(any());
    }

    @Test
    void unusedAccountCanBeDeleted() {
        service.delete(user.getId(), actor);
        verify(users).delete(user);
        verify(sessions).revokeAll(user.getId());
    }

    @Test
    void missingAccountReturnsNotFound() {
        assertThatThrownBy(() -> service.findById(UUID.randomUUID()))
                .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getCode()).isEqualTo("USER_NOT_FOUND"));
    }
}
