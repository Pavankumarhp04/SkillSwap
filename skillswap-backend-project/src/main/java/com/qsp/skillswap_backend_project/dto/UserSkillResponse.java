package com.qsp.skillswap_backend_project.dto;

import com.qsp.skillswap_backend_project.enums.SkillType;

public record UserSkillResponse(Long id, Long skillId, String skillName, SkillType type) {

}
