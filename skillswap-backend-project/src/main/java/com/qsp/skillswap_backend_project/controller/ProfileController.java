package com.qsp.skillswap_backend_project.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.qsp.skillswap_backend_project.dto.ProfileResponse;
import com.qsp.skillswap_backend_project.dto.SkillRequest;
import com.qsp.skillswap_backend_project.dto.UpdateProfileRequest;
import com.qsp.skillswap_backend_project.dto.UserSkillResponse;
import com.qsp.skillswap_backend_project.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me")
public class ProfileController {

	@Autowired
	private UserService userService;

    @GetMapping
    public ProfileResponse me(Principal principal) {
        return userService.getProfile(principal.getName());
    }

    @PutMapping
    public ProfileResponse updateName(Principal principal, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateName(principal.getName(), request);
    }

    @GetMapping("/skills")
    public List<UserSkillResponse> mySkills(Principal principal) {
        return userService.getMySkills(principal.getName());
    }

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSkillResponse addSkill(Principal principal, @Valid @RequestBody SkillRequest request) {
        return userService.addSkill(principal.getName(), request);
    }

    @DeleteMapping("/skills/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSkill(Principal principal, @PathVariable Long id) {
        userService.removeSkill(principal.getName(), id);
    }
    
}
