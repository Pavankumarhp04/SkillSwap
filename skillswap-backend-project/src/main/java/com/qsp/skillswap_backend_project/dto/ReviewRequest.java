package com.qsp.skillswap_backend_project.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(@NotNull @Min(1) @Max(5) Integer rating,
		@Size(max = 500, message = "must be at most 500 characters") String comment) {

}
