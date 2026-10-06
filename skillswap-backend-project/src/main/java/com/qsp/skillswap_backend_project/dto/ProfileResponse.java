package com.qsp.skillswap_backend_project.dto;

import java.util.List;

import com.qsp.skillswap_backend_project.enums.Role;

public record ProfileResponse(Long id, String name, String email, Role role, int creditBalance,
		List<String> teachSkills, List<String> learnSkills) {

}
