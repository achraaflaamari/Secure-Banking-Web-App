package com.kma.backend.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Long> revokedUntilEpochMs = new ConcurrentHashMap<>();

    public void revoke(String jti, long expiresAtEpochMs) {
        if (jti == null) {
            return;
        }
        revokedUntilEpochMs.put(jti, expiresAtEpochMs);
    }

    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        Long until = revokedUntilEpochMs.get(jti);
        if (until == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (until < now) {
            revokedUntilEpochMs.remove(jti);
            return false;
        }
        return true;
    }
}
