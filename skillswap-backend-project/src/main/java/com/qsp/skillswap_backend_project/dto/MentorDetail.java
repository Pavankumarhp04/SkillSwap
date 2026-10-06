package com.qsp.skillswap_backend_project.dto;

import java.util.List;

public record MentorDetail(MentorSummary mentor, List<SlotResponse> openSlots) {

}
