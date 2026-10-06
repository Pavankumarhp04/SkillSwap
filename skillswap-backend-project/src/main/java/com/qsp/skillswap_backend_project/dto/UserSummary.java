package com.qsp.skillswap_backend_project.dto;

import com.qsp.skillswap_backend_project.enums.Role;

public record UserSummary(Long id, String name, String email, Role role, int creditBalance) {

}
