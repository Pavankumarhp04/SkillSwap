package com.qsp.skillswap_backend_project.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qsp.skillswap_backend_project.dto.ReviewRequest;
import com.qsp.skillswap_backend_project.dto.ReviewResponse;
import com.qsp.skillswap_backend_project.entity.LearningSession;
import com.qsp.skillswap_backend_project.entity.Review;
import com.qsp.skillswap_backend_project.entity.User;
import com.qsp.skillswap_backend_project.enums.Role;
import com.qsp.skillswap_backend_project.enums.SessionStatus;
import com.qsp.skillswap_backend_project.exception.AccessDeniedException;
import com.qsp.skillswap_backend_project.exception.ResourceNotFoundException;
import com.qsp.skillswap_backend_project.repository.LearningSessionRepository;
import com.qsp.skillswap_backend_project.repository.ReviewRepository;
import com.qsp.skillswap_backend_project.repository.UserRepository;

@Service
public class ReviewService {

	private static final int LIST_LIMIT = 20;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private LearningSessionRepository sessionRepository;
	@Autowired
	private ReviewRepository reviewRepository;

	/**
	 * The mentor or the learner of a COMPLETED session can review it, once each. A
	 * learner's review is the mentor's public rating; a mentor's review of the
	 * learner is kept for the record.
	 */
	@Transactional
	public ReviewResponse create(String email, Long sessionId, ReviewRequest request) {
		User reviewer = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		LearningSession session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new ResourceNotFoundException("Session not found"));

		boolean isMentor = session.getMentor().getId().equals(reviewer.getId());
		boolean isLearner = session.getLearner().getId().equals(reviewer.getId());
		if (!isMentor && !isLearner) {
			throw new AccessDeniedException("You were not part of this session");
		}
		if (session.getStatus() != SessionStatus.COMPLETED) {
			throw new IllegalStateException("You can review a session only after it is completed");
		}
		if (reviewRepository.existsBySessionIdAndReviewerId(sessionId, reviewer.getId())) {
			throw new IllegalStateException("You already reviewed this session");
		}

		Review review = new Review();
		review.setSession(session);
		review.setReviewer(reviewer);
		review.setRating(request.rating());
		String comment = request.comment() == null ? "" : request.comment().trim();
		review.setComment(comment.isEmpty() ? null : comment);

		return toResponse(reviewRepository.save(review));
	}

	@Transactional(readOnly = true)
	public List<ReviewResponse> mentorReviews(Long mentorId) {
		userRepository.findById(mentorId).filter(u -> u.getRole() == Role.MENTOR)
				.orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

		return reviewRepository.findMentorReviews(mentorId, PageRequest.of(0, LIST_LIMIT)).stream()
				.map(this::toResponse).toList();
	}

	private ReviewResponse toResponse(Review r) {
		return new ReviewResponse(r.getId(), r.getRating(), r.getComment(), r.getReviewer().getName(),
				r.getCreatedAt());
	}
}