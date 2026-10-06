package com.qsp.skillswap_backend_project.dto;

import java.util.Map;

public record ApiError(int status, String message, Map<String, String> errors) {

}
