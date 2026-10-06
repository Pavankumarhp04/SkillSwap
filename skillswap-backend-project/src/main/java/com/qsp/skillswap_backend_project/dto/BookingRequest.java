package com.qsp.skillswap_backend_project.dto;

import jakarta.validation.constraints.NotNull;

public record BookingRequest(@NotNull Long slotId) {

}
