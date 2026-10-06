package com.qsp.skillswap_backend_project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qsp.skillswap_backend_project.dto.SkillResponse;
import com.qsp.skillswap_backend_project.service.UserService;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

	@Autowired
	private UserService userService;
	
	@GetMapping
    public List<SkillResponse> allSkills() {
        return userService.getAllSkills();
    }
	
}
