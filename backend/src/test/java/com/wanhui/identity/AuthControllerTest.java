package com.wanhui.identity;

import com.wanhui.common.exception.BusinessException;
import com.wanhui.common.exception.GlobalExceptionHandler;
import com.wanhui.common.trace.TraceIdFilter;
import com.wanhui.identity.controller.AuthController;
import com.wanhui.identity.dto.CurrentUserResponse;
import com.wanhui.identity.dto.LoginRequest;
import com.wanhui.identity.dto.RegisterRequest;
import com.wanhui.identity.dto.UserSummary;
import com.wanhui.identity.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService, CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new TraceIdFilter())
                .build();
    }

    @Test
    void registersValidUser() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new UserSummary("7", "demo_user", "LOGIN"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "demo_user",
                                  "email": "demo@example.com",
                                  "password": "Passw0rd!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("7"))
                .andExpect(jsonPath("$.data.next").value("LOGIN"));
    }

    @Test
    void rejectsDuplicateEmailOrUsernameWithConflict() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new BusinessException(HttpStatus.CONFLICT, "CONFLICT", "用户名或邮箱已注册"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "demo_user",
                                  "email": "demo@example.com",
                                  "password": "Passw0rd!"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    @Test
    void rejectsWeakPasswordWithValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "demo_user",
                                  "email": "demo@example.com",
                                  "password": "weak"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("password"));
    }

    @Test
    void logsInWithValidCredentials() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(currentUser());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "account": "demo_user",
                                  "password": "Passw0rd!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("demo_user"))
                .andExpect(jsonPath("$.data.roles[0]").value("USER"));
    }

    @Test
    void rejectsInvalidCredentialsWithUnifiedMessage() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessException(
                        HttpStatus.UNAUTHORIZED,
                        "INVALID_CREDENTIALS",
                        "账号或密码不正确"
                ));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "account": "missing_user",
                                  "password": "Passw0rd!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.error.message").value("账号或密码不正确"));
    }

    private CurrentUserResponse currentUser() {
        return new CurrentUserResponse(
                "7",
                "demo_user",
                "测试用户",
                "avatar_01",
                "dem***@example.com",
                null,
                List.of("USER"),
                List.of("POST_CREATE", "POST_DELETE_OWN", "POST_INTERACT")
        );
    }
}
