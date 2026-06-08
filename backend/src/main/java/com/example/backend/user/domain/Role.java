package com.example.backend.user.domain;

public enum Role {
    ROLE_ADMIN,
    ROLE_ANALYSTE_SECURITE;

    public static Role fromString(String roleStr) {
        if (roleStr == null) {
            return ROLE_ANALYSTE_SECURITE;
        }
        String normalized = roleStr.trim().toUpperCase();
        if ("ROLE_ANALYSTE".equals(normalized) || "ANALYSTE".equals(normalized)) {
            return ROLE_ANALYSTE_SECURITE;
        }
        if ("ADMIN".equals(normalized) || "ROLE_ADMIN".equals(normalized)) {
            return ROLE_ADMIN;
        }
        try {
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return ROLE_ANALYSTE_SECURITE;
        }
    }
}
