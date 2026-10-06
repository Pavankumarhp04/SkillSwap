package com.qsp.skillswap_backend_project.dto;

import java.util.List;

public record AdminStats(long totalUsers, long learners, long mentors, long admins, long totalSessions,
		long bookedSessions, long completedSessions, long totalReviews, Double averageRating, long openSlots,
		long creditsInCirculation, List<TopMentor> topMentors) {

}
