package com.qsp.skillswap_backend_project.dto;

import java.time.LocalDateTime;

public record ReviewResponse(Long id, int rating, String comment, String reviewerName, LocalDateTime createdAt) {

}
