package com.umaso.mantenimientos.modules.auth.controller;

import com.umaso.mantenimientos.config.SecurityProperties;
import com.umaso.mantenimientos.modules.auth.dto.request.LoginRequest;
import com.umaso.mantenimientos.modules.auth.dto.response.AuthResponse;
import com.umaso.mantenimientos.modules.auth.service.AuthService;
import com.umaso.mantenimientos.modules.auth.service.IssuedSession;
import com.umaso.mantenimientos.modules.auth.service.RefreshCookieService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerCookieFlowTest {
    private AuthService authService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        SecurityProperties properties = new SecurityProperties(
                "https://issuer.test", "audience", Duration.ofMinutes(15), Duration.ofDays(7),
                "", "", true, List.of("http://localhost:5173"),
                new SecurityProperties.RefreshCookie("refresh_token", false, "Lax", "/api/auth"));
        AuthController controller = new AuthController(
                authService, new RefreshCookieService(properties), properties);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void loginCookieSurvivesReloadAndIsUsedByRefresh() throws Exception {
        Instant expiration = Instant.now().plus(Duration.ofDays(7));
        AuthResponse loginResponse = new AuthResponse("access-1", "Bearer", 900, null);
        AuthResponse refreshResponse = new AuthResponse("access-2", "Bearer", 900, null);
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new IssuedSession(loginResponse, "refresh-1", expiration));
        when(authService.refresh("refresh-1"))
                .thenReturn(new IssuedSession(refreshResponse, "refresh-2", expiration));

        MockHttpServletResponse login = mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"correo\":\"user@example.com\",\"contrasena\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", org.hamcrest.Matchers.nullValue()))
                .andReturn().getResponse();

        String setCookie = login.getHeader("Set-Cookie");
        assertThat(setCookie).contains("refresh_token=refresh-1", "Path=/api/auth", "Max-Age=",
                "HttpOnly", "SameSite=Lax").doesNotContain("Secure", "Domain=");

        Cookie storedCookie = new Cookie("refresh_token", "refresh-1");
        mvc.perform(post("/api/auth/refresh").cookie(storedCookie))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString(
                        "refresh_token=refresh-2")));

        verify(authService).refresh("refresh-1");
    }
}
