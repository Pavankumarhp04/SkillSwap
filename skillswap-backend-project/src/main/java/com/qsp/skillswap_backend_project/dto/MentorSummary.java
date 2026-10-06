package com.qsp.skillswap_backend_project.dto;

import java.util.List;

public record MentorSummary( Long id,
        String name,
        List<String> teachSkills,
        Double averageRating,
        long reviewCount,
        long openSlots) {

}
