package com.pabloverdejo.stockpilot.auth;

import java.util.List;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String username,
        List<String> roles) {
}
