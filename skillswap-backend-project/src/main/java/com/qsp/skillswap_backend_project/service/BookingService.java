package com.qsp.skillswap_backend_project.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.qsp.skillswap_backend_project.entity.AvailabilitySlot;
import com.qsp.skillswap_backend_project.entity.CreditTransaction;
import com.qsp.skillswap_backend_project.entity.LearningSession;
import com.qsp.skillswap_backend_project.entity.User;
import com.qsp.skillswap_backend_project.enums.SessionStatus;
import com.qsp.skillswap_backend_project.enums.TransactionType;
import com.qsp.skillswap_backend_project.exception.ResourceNotFoundException;
import com.qsp.skillswap_backend_project.repository.AvailabilitySlotRepository;
import com.qsp.skillswap_backend_project.repository.CreditTransactionRepository;
import com.qsp.skillswap_backend_project.repository.LearningSessionRepository;
import com.qsp.skillswap_backend_project.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class BookingService {

	private static final int SESSION_COST = 1;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AvailabilitySlotRepository slotRepository;
	@Autowired
	private LearningSessionRepository sessionRepository;
	@Autowired
	private CreditTransactionRepository transactionRepository;

	@Value("${skillswap.allow-early-completion:false}")
	private boolean allowEarlyCompletion;

	@Transactional
	public LearningSession bookSession(Long learnerId, Long slotId) {

		AvailabilitySlot slot = slotRepository.findByIdForUpdate(slotId)
				.orElseThrow(() -> new IllegalArgumentException("Slot not found"));

		if (slot.isBooked()) {
			throw new IllegalStateException("This slot is already booked");
		}

		if (slot.getStartTime().isBefore(LocalDateTime.now())) {

			throw new IllegalStateException("This slot is in the past");
		}

		User learner = userRepository.findById(learnerId)
				.orElseThrow(() -> new IllegalArgumentException("Learner not found"));

		User mentor = slot.getMentor();

		if (mentor.getId().equals(learner.getId())) {
			throw new IllegalStateException("You cannot book your own slot");
		}

		if (learner.getCreditBalance() < SESSION_COST) {
			throw new IllegalStateException("Not enough credits");
		}

		slot.setBooked(true);

		learner.setCreditBalance(learner.getCreditBalance() - SESSION_COST);

		LearningSession session = new LearningSession();

		session.setMentor(mentor);
		session.setLearner(learner);
		session.setSlot(slot);

		session.setMeetingLink("https://meet.jit.si/skillswap-" + UUID.randomUUID());

		session = sessionRepository.save(session);

		recordTransaction(learner, session, SESSION_COST, TransactionType.SPEND);

		return session;
	}

	@Transactional
	public LearningSession completeSession(Long sessionId, Long mentorId) {

		LearningSession session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new IllegalArgumentException("Session not found"));

		if (!session.getMentor().getId().equals(mentorId)) {

			throw new IllegalStateException("Only the mentor can complete this session");
		}

		if (session.getStatus() != SessionStatus.BOOKED) {

			throw new IllegalStateException("Session is not in BOOKED state");
		}

		if (!allowEarlyCompletion && session.getSlot().getStartTime().isAfter(LocalDateTime.now())) {
			throw new IllegalStateException("You can complete a session only after it has started");
		}

		// Lock the mentor row so two completions at the same time cannot overwrite each
		// other's credit
		User mentor = userRepository.findByIdForUpdate(mentorId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		session.setStatus(SessionStatus.COMPLETED);

		mentor.setCreditBalance(mentor.getCreditBalance() + SESSION_COST);

		recordTransaction(mentor, session, SESSION_COST, TransactionType.EARN);

		return session;
	}

	private void recordTransaction(User user, LearningSession session, int amount, TransactionType type) {

		CreditTransaction tx = new CreditTransaction();

		tx.setUser(user);
		tx.setSession(session);
		tx.setAmount(amount);
		tx.setType(type);

		transactionRepository.save(tx);
	}
}
