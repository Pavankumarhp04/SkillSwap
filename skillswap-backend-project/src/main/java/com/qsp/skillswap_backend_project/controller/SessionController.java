package com.qsp.skillswap_backend_project.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.qsp.skillswap_backend_project.dto.BookingRequest;
import com.qsp.skillswap_backend_project.dto.ReviewRequest;
import com.qsp.skillswap_backend_project.dto.ReviewResponse;
import com.qsp.skillswap_backend_project.dto.SessionResponse;
import com.qsp.skillswap_backend_project.service.ReviewService;
import com.qsp.skillswap_backend_project.service.SessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

	@Autowired
	private SessionService sessionService;
	@Autowired
	private ReviewService reviewService;

	// Book a slot (the logged-in user is the learner and pays 1 credit)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SessionResponse book(Principal principal, @Valid @RequestBody BookingRequest request) {
		return sessionService.book(principal.getName(), request.slotId());
	}

	// Every session where the logged-in user is the mentor or the learner
	@GetMapping("/me")
	public List<SessionResponse> mySessions(Principal principal) {
		return sessionService.mySessions(principal.getName());
	}

	// The mentor marks a session as done and earns 1 credit
	@PostMapping("/{id}/complete")
	public SessionResponse complete(Principal principal, @PathVariable Long id) {
		return sessionService.complete(principal.getName(), id);
	}

	@PostMapping("/{id}/reviews")
	@ResponseStatus(HttpStatus.CREATED)
	public ReviewResponse review(Principal principal, @PathVariable Long id,
			@Valid @RequestBody ReviewRequest request) {
		return reviewService.create(principal.getName(), id, request);
	}
}
