package com.kma.backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Base64;

@RestController
public class CsrfController {

    private final SecureRandom secureRandom = new SecureRandom();

    @GetMapping("/api/csrf")
    public ResponseEntity<Void> csrf(HttpServletResponse response) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        String cookie = "XSRF-TOKEN=" + token + "; Path=/; SameSite=Lax";
        response.addHeader("Set-Cookie", cookie);
        return ResponseEntity.ok().build();
    }
}
