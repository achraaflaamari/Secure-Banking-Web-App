package com.kma.backend.controller;

import com.kma.backend.dto.AuthResponse;
import com.kma.backend.dto.ErrorResponse;
import com.kma.backend.dto.ForgotPasswordRequest;
import com.kma.backend.dto.LoginRequest;
import com.kma.backend.dto.MeResponse;
import com.kma.backend.dto.OtpVerifyRequest;
import com.kma.backend.dto.RegisterRequest;
import com.kma.backend.dto.ResetPasswordRequest;
import com.kma.backend.security.TokenBlacklistService;
import com.kma.backend.security.TokenUtil;
import com.kma.backend.service.AuthService;
import com.kma.backend.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        if (response.getToken() != null || "OTP_SENT".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/register/verify")
    public ResponseEntity<AuthResponse> verifyRegister(@Valid @RequestBody OtpVerifyRequest request) {
        AuthResponse response = authService.verifySignupOtp(request.getEmail(), request.getCode());
        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        if (response.getToken() != null || "OTP_REQUIRED".equals(response.getMessage())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/login/verify")
    public ResponseEntity<AuthResponse> verifyLogin(@Valid @RequestBody OtpVerifyRequest request) {
        AuthResponse response = authService.verifyLoginOtp(request.getEmail(), request.getCode());
        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        AuthResponse response = authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        AuthResponse response = authService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        if ("Password updated".equals(response.getMessage())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request) {
        String token = TokenUtil.extractBearerToken(request);
        if (token != null && jwtUtil.isTokenValid(token) && !jwtUtil.isTokenExpired(token)) {
            String jti = jwtUtil.extractJti(token);
            long exp = jwtUtil.extractExpirationEpochMs(token);
            tokenBlacklistService.revoke(jti, exp);
        }
        return ResponseEntity.ok(new AuthResponse(null, null, null, "Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication, HttpServletRequest request) {
        String token = TokenUtil.extractBearerToken(request);
        if (token == null || !jwtUtil.isTokenValid(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Unauthorized"));
        }
        String jti = jwtUtil.extractJti(token);
        if (tokenBlacklistService.isRevoked(jti)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Unauthorized"));
        }
        String username = authentication.getName();
        String role = jwtUtil.extractRole(token);
        return ResponseEntity.ok(new MeResponse(username, role));
    }
}
