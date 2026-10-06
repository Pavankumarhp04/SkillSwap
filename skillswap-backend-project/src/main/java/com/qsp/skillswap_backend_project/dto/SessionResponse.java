package com.qsp.skillswap_backend_project.dto;

import java.time.LocalDateTime;

import com.qsp.skillswap_backend_project.enums.SessionStatus;

public record SessionResponse(Long id, Long mentorId, String mentorName, Long learnerId, String learnerName,
		LocalDateTime startTime, LocalDateTime endTime, SessionStatus status, String meetingLink, String myRole,
		boolean reviewed) {

}
