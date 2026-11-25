package com.wanhui.identity.controller;

import com.wanhui.common.api.ApiResponse;
import com.wanhui.identity.dto.CurrentUserResponse;
import com.wanhui.identity.dto.LoginRequest;
import com.wanhui.identity.dto.RegisterRequest;
import com.wanhui.identity.dto.UserSummary;
import com.wanhui.identity.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthController(AuthService authService, CsrfTokenRepository csrfTokenRepository) {
        this.authService = authService;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserSummary>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authService.register(request)));
    }

    @PostMapping("/login")
    public ApiResponse<CurrentUserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        CurrentUserResponse user = authService.login(request);
        HttpSession session = servletRequest.getSession(true);
        servletRequest.changeSessionId();

        List<GrantedAuthority> authorities = new ArrayList<>();
        user.roles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        user.permissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.id(),
                null,
                authorities
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        CsrfToken csrfToken = csrfTokenRepository.generateToken(servletRequest);
        csrfTokenRepository.saveToken(csrfToken, servletRequest, servletResponse);
        return ApiResponse.success(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ApiResponse.success(null);
        }
        try {
            return ApiResponse.success(authService.currentUser(Long.valueOf(authentication.getName())));
        } catch (NumberFormatException exception) {
            return ApiResponse.success(null);
        }
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }
}
