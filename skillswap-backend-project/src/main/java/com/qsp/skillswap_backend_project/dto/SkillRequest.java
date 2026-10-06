package com.qsp.skillswap_backend_project.dto;

import com.qsp.skillswap_backend_project.enums.SkillType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkillRequest(@NotBlank @Size(max = 50) String skillName, @NotNull SkillType type) {

}
