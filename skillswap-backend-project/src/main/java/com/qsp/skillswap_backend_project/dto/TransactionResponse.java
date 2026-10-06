package com.qsp.skillswap_backend_project.dto;

import java.time.LocalDateTime;

import com.qsp.skillswap_backend_project.enums.TransactionType;

public record TransactionResponse(Long id, TransactionType type, int amount, Long sessionId, String description,
		LocalDateTime createdAt) {

}
