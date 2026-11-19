package com.example.hospital_manager.dto;

import com.example.hospital_manager.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
    @NotBlank String username,
    @NotBlank String password,
    String fullName,
    String name,
    @NotNull Role role
) {
    public String getEffectiveFullName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        if (name != null && !name.isBlank()) {
            return name;
        }
        return username;
    }
}
