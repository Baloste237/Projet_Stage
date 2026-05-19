package com.example.backend.user.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }
        return role.name();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        // Handle legacy roles seamlessly
        if ("ROLE_ANALYSTE".equals(dbData)) {
            return Role.ROLE_ANALYSTE_SECURITE;
        }
        
        try {
            return Role.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Fallback for unknown roles to prevent crashing the whole app
            return Role.ROLE_ANALYSTE_SECURITE;
        }
    }
}
