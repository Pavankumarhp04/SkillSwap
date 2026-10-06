package com.qsp.skillswap_backend_project.dto;

import com.qsp.skillswap_backend_project.enums.Role;

import jakarta.validation.constraints.NotNull;

public record RoleChangeRequest(@NotNull Role role) {

}
