package com.qsp.skillswap_backend_project.dto;

import java.time.LocalDateTime;

public record AdminReviewRow(Long id, int rating, String comment, String reviewerName, String mentorName,
		String learnerName, LocalDateTime createdAt) {

}
