package com.qsp.skillswap_backend_project.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qsp.skillswap_backend_project.dto.SessionResponse;
import com.qsp.skillswap_backend_project.dto.TransactionResponse;
import com.qsp.skillswap_backend_project.dto.WalletResponse;
import com.qsp.skillswap_backend_project.entity.CreditTransaction;
import com.qsp.skillswap_backend_project.entity.LearningSession;
import com.qsp.skillswap_backend_project.entity.User;
import com.qsp.skillswap_backend_project.enums.TransactionType;
import com.qsp.skillswap_backend_project.exception.ResourceNotFoundException;
import com.qsp.skillswap_backend_project.repository.CreditTransactionRepository;
import com.qsp.skillswap_backend_project.repository.LearningSessionRepository;
import com.qsp.skillswap_backend_project.repository.ReviewRepository;
import com.qsp.skillswap_backend_project.repository.UserRepository;

@Service
public class SessionService {

	private static final int HISTORY_LIMIT = 50;

	@Autowired
	private BookingService bookingService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private LearningSessionRepository sessionRepository;
	@Autowired
	private CreditTransactionRepository transactionRepository;
	@Autowired
	private ReviewRepository reviewRepository;

	@Transactional
	public SessionResponse book(String learnerEmail, Long slotId) {
		User learner = findUser(learnerEmail);
		return toResponse(bookingService.bookSession(learner.getId(), slotId), learner.getId(), false);
	}

	@Transactional
	public SessionResponse complete(String mentorEmail, Long sessionId) {
		User mentor = findUser(mentorEmail);
		return toResponse(bookingService.completeSession(sessionId, mentor.getId()), mentor.getId(), false);
	}

	@Transactional(readOnly = true)
	public List<SessionResponse> mySessions(String email) {
		User user = findUser(email);
		Set<Long> reviewed = new HashSet<>(reviewRepository.findSessionIdsReviewedBy(user.getId()));
		return sessionRepository.findAllForUser(user.getId()).stream()
				.map(s -> toResponse(s, user.getId(), reviewed.contains(s.getId()))).toList();
	}

	@Transactional(readOnly = true)
	public WalletResponse wallet(String email) {
		User user = findUser(email);
		List<TransactionResponse> history = transactionRepository
				.findHistory(user.getId(), PageRequest.of(0, HISTORY_LIMIT)).stream().map(this::toTransaction).toList();
		return new WalletResponse(user.getCreditBalance(), history);
	}

	private SessionResponse toResponse(LearningSession s, Long viewerId, boolean reviewed) {
		String myRole = s.getMentor().getId().equals(viewerId) ? "MENTOR" : "LEARNER";

		return new SessionResponse(s.getId(), s.getMentor().getId(), s.getMentor().getName(), s.getLearner().getId(),
				s.getLearner().getName(), s.getSlot().getStartTime(), s.getSlot().getEndTime(), s.getStatus(),
				s.getMeetingLink(), myRole, reviewed);
	}

	private TransactionResponse toTransaction(CreditTransaction t) {
		LearningSession s = t.getSession();
		String description;
		if (s == null) {
			description = t.getType() == TransactionType.EARN ? "Credit earned" : "Credit spent";
		} else if (t.getType() == TransactionType.SPEND) {
			description = "Session with " + s.getMentor().getName();
		} else {
			description = "Taught " + s.getLearner().getName();
		}
		return new TransactionResponse(t.getId(), t.getType(), t.getAmount(), s == null ? null : s.getId(), description,
				t.getCreatedAt());
	}

	private User findUser(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}
