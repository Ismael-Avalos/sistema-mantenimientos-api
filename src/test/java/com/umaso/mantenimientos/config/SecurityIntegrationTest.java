package com.umaso.mantenimientos.config;

import com.umaso.mantenimientos.modules.assets.controller.AssetController;
import com.umaso.mantenimientos.modules.assets.service.AssetService;
import com.umaso.mantenimientos.modules.auth.service.JwtTokenService;
import com.umaso.mantenimientos.modules.maintenances.controller.MaintenanceController;
import com.umaso.mantenimientos.modules.maintenances.service.MaintenanceService;
import com.umaso.mantenimientos.modules.roles.entity.Role;
import com.umaso.mantenimientos.modules.users.entity.User;
import com.umaso.mantenimientos.modules.users.repository.UserRepository;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AssetController.class, MaintenanceController.class}, properties = {
        "app.security.issuer=https://issuer.test", "app.security.audience=audience",
        "app.security.access-token-ttl=15m", "app.security.refresh-token-ttl=7d",
        "app.security.allow-ephemeral-dev-keys=true",
        "app.security.cors-allowed-origins[0]=http://localhost:5173",
        "app.security.refresh-cookie.name=refresh_token", "app.security.refresh-cookie.secure=false",
        "app.security.refresh-cookie.same-site=Lax", "app.security.refresh-cookie.path=/api/auth"
})
@Import({SecurityConfig.class, CurrentUserSecurityFilter.class, AuthOriginFilter.class, SecurityErrorWriter.class,
        JwtTokenService.class})
class SecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JwtTokenService jwtTokens;
    @MockitoBean AssetService assetService;
    @MockitoBean MaintenanceService maintenanceService;
    @MockitoBean UserRepository userRepository;
    private User admin;
    private User technician;

    @BeforeEach
    void setUp() {
        admin = user("ADMINISTRADOR", false);
        technician = user("TECNICO", false);
        when(userRepository.findByIdWithRole(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            if (id.equals(admin.getId())) return Optional.of(admin);
            if (id.equals(technician.getId())) return Optional.of(technician);
            return Optional.empty();
        });
        when(assetService.findAll()).thenReturn(List.of());
    }

    @Test
    void protectedEndpointWithoutTokenReturnsProblem401() throws Exception {
        mvc.perform(get("/maintenances/assets"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("AUTH_TOKEN_MISSING"));
    }

    @Test
    void internalErrorDispatchIsNotReplacedByMissingTokenResponse() throws Exception {
        mvc.perform(servletContext -> {
                    var request = get("/error").buildRequest(servletContext);
                    request.setDispatcherType(DispatcherType.ERROR);
                    request.setAttribute("jakarta.servlet.error.status_code", 500);
                    request.setAttribute("jakarta.servlet.error.request_uri", "/maintenances/assets");
                    return request;
                })
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void wrongRoleIs403AndAllowedRoleSucceeds() throws Exception {
        String body = "{\"codigoInventario\":\"EQ-1\",\"nombre\":\"Equipo\",\"categoriaId\":\""
                + UUID.randomUUID() + "\"}";
        mvc.perform(post("/maintenances/assets").header("Authorization", bearer(technician))
                        .contentType("application/json").content(body))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_ACCESS_DENIED"));
        mvc.perform(post("/maintenances/assets").header("Authorization", bearer(admin))
                        .contentType("application/json").content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void requiredPasswordChangeBlocksFunctionalEndpoints() throws Exception {
        User blocked = user("ADMIN", true);
        when(userRepository.findByIdWithRole(blocked.getId())).thenReturn(Optional.of(blocked));
        mvc.perform(get("/maintenances/assets").header("Authorization", bearer(blocked)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_PASSWORD_CHANGE_REQUIRED"));
    }

    @Test
    void corsAcceptsConfiguredOriginAndRejectsOtherOrigin() throws Exception {
        mvc.perform(options("/maintenances/assets").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Headers", "Authorization"));
        mvc.perform(options("/maintenances/assets").header("Origin", "http://denied.test")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAssetsAcceptsDatabaseAdministratorAndTechnicianRoles() throws Exception {
        mvc.perform(get("/maintenances/assets").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
        mvc.perform(get("/maintenances/assets").header("Authorization", bearer(technician)))
                .andExpect(status().isOk());
    }

    @Test
    void technicianCanEditMaintenanceButOnlyAdministratorCanDeleteIt() throws Exception {
        UUID maintenanceId = UUID.randomUUID();
        String body = """
                {
                  "responsableId": null,
                  "tipo": "PREVENTIVO",
                  "solicitanteNombre": "Solicitante",
                  "solicitanteCorreo": "solicitante@example.com",
                  "solicitanteTelefono": "7000-0000",
                  "unidad": "Informática",
                  "descripcionFalla": "Falla",
                  "actividadesRealizadas": "Diagnóstico",
                  "observacionesTecnicas": null,
                  "recomendaciones": null,
                  "costo": 0,
                  "fecha": "2026-08-29T08:00:00",
                  "fechaEntrega": "2026-08-29T09:00:00"
                }
                """;

        mvc.perform(put("/api/mantenimientos/{id}", maintenanceId)
                        .header("Authorization", bearer(technician))
                        .contentType("application/json").content(body))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/mantenimientos/{id}", maintenanceId)
                        .header("Authorization", bearer(technician)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/mantenimientos/{id}", maintenanceId)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
    }

    private String bearer(User user) { return "Bearer " + jwtTokens.issue(user); }

    private User user(String role, boolean changeRequired) {
        return User.builder().id(UUID.randomUUID()).nombre(role).correo(role.toLowerCase() + "@example.com")
                .contrasena("hash").activo(true).debeCambiarContrasena(changeRequired).securityVersion(0)
                .rol(Role.builder().nombre(role).build()).build();
    }
}
