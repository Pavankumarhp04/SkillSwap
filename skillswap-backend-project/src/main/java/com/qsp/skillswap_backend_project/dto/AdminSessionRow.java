package com.qsp.skillswap_backend_project.dto;

import java.time.LocalDateTime;

import com.qsp.skillswap_backend_project.enums.SessionStatus;

public record AdminSessionRow(Long id, String mentorName, String learnerName, LocalDateTime startTime,
		SessionStatus status) {

}
