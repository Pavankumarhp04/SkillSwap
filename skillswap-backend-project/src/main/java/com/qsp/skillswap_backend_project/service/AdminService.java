package com.qsp.skillswap_backend_project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qsp.skillswap_backend_project.dto.AdminReviewRow;
import com.qsp.skillswap_backend_project.dto.AdminSessionRow;
import com.qsp.skillswap_backend_project.dto.AdminStats;
import com.qsp.skillswap_backend_project.dto.TopMentor;
import com.qsp.skillswap_backend_project.dto.UserSummary;
import com.qsp.skillswap_backend_project.entity.User;
import com.qsp.skillswap_backend_project.enums.Role;
import com.qsp.skillswap_backend_project.enums.SessionStatus;
import com.qsp.skillswap_backend_project.exception.ResourceNotFoundException;
import com.qsp.skillswap_backend_project.repository.AvailabilitySlotRepository;
import com.qsp.skillswap_backend_project.repository.LearningSessionRepository;
import com.qsp.skillswap_backend_project.repository.ReviewRepository;
import com.qsp.skillswap_backend_project.repository.UserRepository;

@Service
public class AdminService {

	private static final int RECENT_LIMIT = 20;
	private static final int TOP_MENTORS = 5;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private LearningSessionRepository sessionRepository;
	@Autowired
	private ReviewRepository reviewRepository;
	@Autowired
	private AvailabilitySlotRepository slotRepository;

	@Transactional(readOnly = true)
	public AdminStats stats() {
		List<ReviewRepository.MentorRatingStats> top = reviewRepository.topMentorStats(PageRequest.of(0, TOP_MENTORS));
		Map<Long, String> names = userRepository
				.findAllById(top.stream().map(ReviewRepository.MentorRatingStats::getMentorId).toList()).stream()
				.collect(Collectors.toMap(User::getId, User::getName));

		List<TopMentor> topMentors = top.stream().map(t -> new TopMentor(t.getMentorId(),
				names.getOrDefault(t.getMentorId(), "Unknown"), round1(t.getAverageRating()), t.getReviewCount()))
				.toList();

		Double average = reviewRepository.overallAverageRating();

		return new AdminStats(userRepository.count(), userRepository.countByRole(Role.LEARNER),
				userRepository.countByRole(Role.MENTOR), userRepository.countByRole(Role.ADMIN),
				sessionRepository.count(), sessionRepository.countByStatus(SessionStatus.BOOKED),
				sessionRepository.countByStatus(SessionStatus.COMPLETED), reviewRepository.count(),
				average == null ? null : round1(average),
				slotRepository.countByBookedFalseAndStartTimeAfter(LocalDateTime.now()), userRepository.totalCredits(),
				topMentors);
	}

	@Transactional
	public UserSummary changeRole(String adminEmail, Long userId, Role role) {
		User target = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// An admin cannot lock themselves out by demoting their own account
		if (target.getEmail().equals(adminEmail)) {
			throw new IllegalStateException("You cannot change your own role");
		}

		target.setRole(role);
		return new UserSummary(target.getId(), target.getName(), target.getEmail(), target.getRole(),
				target.getCreditBalance());
	}

	@Transactional(readOnly = true)
	public List<AdminSessionRow> recentSessions() {
		return sessionRepository
				.findRecent(PageRequest.of(0, RECENT_LIMIT)).stream().map(s -> new AdminSessionRow(s.getId(),
						s.getMentor().getName(), s.getLearner().getName(), s.getSlot().getStartTime(), s.getStatus()))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<AdminReviewRow> recentReviews() {
		return reviewRepository.findRecentWithDetails(PageRequest.of(0, RECENT_LIMIT)).stream()
				.map(r -> new AdminReviewRow(r.getId(), r.getRating(), r.getComment(), r.getReviewer().getName(),
						r.getSession().getMentor().getName(), r.getSession().getLearner().getName(), r.getCreatedAt()))
				.toList();
	}

	// Moderation: remove an unfair or abusive review (the mentor's average updates
	// automatically)
	@Transactional
	public void deleteReview(Long reviewId) {
		if (!reviewRepository.existsById(reviewId)) {
			throw new ResourceNotFoundException("Review not found");
		}
		reviewRepository.deleteById(reviewId);
	}

	private double round1(double value) {
		return Math.round(value * 10) / 10.0;
	}
}
