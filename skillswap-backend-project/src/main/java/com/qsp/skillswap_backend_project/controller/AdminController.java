package com.qsp.skillswap_backend_project.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.qsp.skillswap_backend_project.dto.AdminReviewRow;
import com.qsp.skillswap_backend_project.dto.AdminSessionRow;
import com.qsp.skillswap_backend_project.dto.AdminStats;
import com.qsp.skillswap_backend_project.dto.RoleChangeRequest;
import com.qsp.skillswap_backend_project.dto.UserSummary;
import com.qsp.skillswap_backend_project.service.AdminService;
import com.qsp.skillswap_backend_project.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	@Autowired
	private UserService userService;
	@Autowired
	private AdminService adminService;

	@GetMapping("/stats")
	public AdminStats stats() {
		return adminService.stats();
	}

	@GetMapping("/users")
	public List<UserSummary> allUsers() {
		return userService.listAllUsers();
	}

	@PutMapping("/users/{id}/role")
	public UserSummary changeRole(Principal principal, @PathVariable Long id,
			@Valid @RequestBody RoleChangeRequest request) {
		return adminService.changeRole(principal.getName(), id, request.role());
	}

	@GetMapping("/sessions")
	public List<AdminSessionRow> recentSessions() {
		return adminService.recentSessions();
	}

	@GetMapping("/reviews")
	public List<AdminReviewRow> recentReviews() {
		return adminService.recentReviews();
	}

	@DeleteMapping("/reviews/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteReview(@PathVariable Long id) {
		adminService.deleteReview(id);
	}

}
