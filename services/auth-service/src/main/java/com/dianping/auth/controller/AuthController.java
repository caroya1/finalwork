package com.dianping.auth.controller;

import com.dianping.auth.dto.LoginRequest;
import com.dianping.auth.dto.LoginResponse;
import com.dianping.auth.dto.AdminLoginRequest;
import com.dianping.auth.dto.LogoutRequest;
import com.dianping.auth.dto.RefreshRequest;
import com.dianping.auth.dto.TokenPairResponse;
import com.dianping.auth.service.AuthService;
import com.dianping.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + response.getToken())
                .header("X-Refresh-Token", response.getRefreshToken())
                .body(ApiResponse.ok(response));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<LoginResponse>> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        LoginResponse response = authService.adminLogin(request.getUsername(), request.getPassword());
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + response.getToken())
                .header("X-Refresh-Token", response.getRefreshToken())
                .body(ApiResponse.ok(response));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenPairResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization,
                                    @RequestBody(required = false) LogoutRequest request) {
        String accessToken = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }
        String refreshToken = request == null ? null : request.getRefreshToken();
        authService.logout(accessToken, refreshToken);
        return ApiResponse.ok(null);
    }
}
