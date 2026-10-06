package com.qsp.skillswap_backend_project.dto;

import java.time.LocalDateTime;

public record SlotResponse(Long id, LocalDateTime startTime, LocalDateTime endTime, boolean booked) {

}
