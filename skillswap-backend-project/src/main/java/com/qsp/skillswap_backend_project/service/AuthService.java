package com.qsp.skillswap_backend_project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qsp.skillswap_backend_project.dto.AuthResponse;
import com.qsp.skillswap_backend_project.dto.LoginRequest;
import com.qsp.skillswap_backend_project.dto.RegisterRequest;
import com.qsp.skillswap_backend_project.entity.User;
import com.qsp.skillswap_backend_project.enums.Role;
import com.qsp.skillswap_backend_project.repository.UserRepository;
import com.qsp.skillswap_backend_project.security.JwtService;

@Service
public class AuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtService jwtService;

	@Transactional
	public AuthResponse register(RegisterRequest request) {

		// ADMIN registration is not allowed
		if (request.role() == Role.ADMIN) {
			throw new IllegalArgumentException("You cannot register as ADMIN");
		}

		String email = request.email().trim().toLowerCase();

		// Check duplicate email
		if (userRepository.existsByEmail(email)) {
			throw new IllegalStateException("This email is already registered");
		}

		// Create user
		User user = new User();

		user.setName(request.name().trim());
		user.setEmail(email);

		// Encrypt password
		user.setPasswordHash(passwordEncoder.encode(request.password()));

		// Default role = LEARNER
		user.setRole(request.role() != null ? request.role() : Role.LEARNER);

		// Save user
		user = userRepository.save(user);

		// Return response with JWT
		return toResponse(user);
	}

	// =========================
	// LOGIN
	// =========================

	public AuthResponse login(LoginRequest request) {

		String email = request.email().trim().toLowerCase();

		// Authenticate email + password
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

		// Find user
		User user = userRepository.findByEmail(email).orElseThrow();

		// Return JWT response
		return toResponse(user);
	}

	// =========================
	// CREATE AUTH RESPONSE
	// =========================

	private AuthResponse toResponse(User user) {

		String token = jwtService.generateToken(user.getEmail(), user.getRole());

		return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
	}
}
