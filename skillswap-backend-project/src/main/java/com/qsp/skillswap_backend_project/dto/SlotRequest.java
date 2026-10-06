package com.qsp.skillswap_backend_project.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record SlotRequest(@NotNull @Future(message = "must be in the future") LocalDateTime startTime,
        @NotNull LocalDateTime endTime) {

}
