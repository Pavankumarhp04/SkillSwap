package com.qsp.skillswap_backend_project.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.qsp.skillswap_backend_project.dto.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// @Valid failed: return one message per field
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
		return build(HttpStatus.BAD_REQUEST, "Validation failed", errors);
	}

	// Broken JSON or an unknown enum value such as "role": "BOSS"
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
		return build(HttpStatus.BAD_REQUEST, "Malformed request body", null);
	}

	// For example GET /api/mentors/abc where an id (a number) was expected
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return build(HttpStatus.BAD_REQUEST, "Invalid value for " + ex.getName(), null);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
		return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", null);
	}
	
	@ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleForbidden(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "This conflicts with existing data (it may already exist)", null);
    }

	// Business-rule conflicts: email already used, slot already booked, not enough
	// credits...
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiError> handleConflict(IllegalStateException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), null);
	}

	private ResponseEntity<ApiError> build(HttpStatus status, String message, Map<String, String> errors) {
		return ResponseEntity.status(status).body(new ApiError(status.value(), message, errors));
	}
}
