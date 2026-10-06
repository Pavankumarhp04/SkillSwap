package com.qsp.skillswap_backend_project.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.qsp.skillswap_backend_project.entity.AvailabilitySlot;
import com.qsp.skillswap_backend_project.entity.LearningSession;
import com.qsp.skillswap_backend_project.entity.Review;
import com.qsp.skillswap_backend_project.entity.Skill;
import com.qsp.skillswap_backend_project.entity.User;
import com.qsp.skillswap_backend_project.entity.UserSkill;
import com.qsp.skillswap_backend_project.enums.Role;
import com.qsp.skillswap_backend_project.enums.SessionStatus;
import com.qsp.skillswap_backend_project.enums.SkillType;
import com.qsp.skillswap_backend_project.repository.AvailabilitySlotRepository;
import com.qsp.skillswap_backend_project.repository.LearningSessionRepository;
import com.qsp.skillswap_backend_project.repository.ReviewRepository;
import com.qsp.skillswap_backend_project.repository.SkillRepository;
import com.qsp.skillswap_backend_project.repository.UserRepository;
import com.qsp.skillswap_backend_project.repository.UserSkillRepository;

import lombok.RequiredArgsConstructor;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final SkillRepository skillRepository;
	private final UserSkillRepository userSkillRepository;
	private final AvailabilitySlotRepository slotRepository;
	private final LearningSessionRepository sessionRepository;
	private final ReviewRepository reviewRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {
		
		if (userRepository.count() > 0) {
			return;
		}

		User learner = createUser("Demo Learner", "learner@gmail.com", "Learner@123", Role.LEARNER);
		createUser("Admin", "admin@gmail.com", "Admin@123", Role.ADMIN);
		seedMentor("Pavan", "pavan@gmail.com", "Pavan@123", List.of("React", "JavaScript"), 5, learner);
		seedMentor("Kumar", "kumar@gmail.com", "Kumar@123", List.of("Java", "Spring Boot", "SQL"), 4, learner);
		seedMentor("Nandan", "nandan@gmail.com", "Nandan@123", List.of("DSA", "Core Java"), 5, learner);
		seedMentor("Karthik", "karthik@gmail.com", "Karthik@123", List.of("Spring Boot", "Hibernate"), 3, learner);
		seedMentor("Vishwas", "vishwas@gmail.com", "Vishwas@123", List.of("HTML", "CSS", "JavaScript"), 5, learner);
	}

	private void seedMentor(String name, String email, String password, List<String> skills, int rating, User learner) {

		User mentor = createUser(name, email, password, Role.MENTOR);

		for (String skillName : skills) {
			UserSkill userSkill = new UserSkill();
			userSkill.setUser(mentor);
			userSkill.setSkill(skill(skillName));
			userSkill.setType(SkillType.TEACH);
			userSkillRepository.save(userSkill);
		}

		int[] hours = { 10, 15, 18 };
		for (int day = 1; day <= 3; day++) {
			LocalDateTime start = LocalDateTime.now().plusDays(day).withHour(hours[day - 1]).withMinute(0).withSecond(0)
					.withNano(0);
			createSlot(mentor, start, false);
		}

		LocalDateTime past = LocalDateTime.now().minusDays(2).withHour(11).withMinute(0).withSecond(0).withNano(0);
		AvailabilitySlot pastSlot = createSlot(mentor, past, true);
		LearningSession session = new LearningSession();
		session.setMentor(mentor);
		session.setLearner(learner);
		session.setSlot(pastSlot);
		session.setStatus(SessionStatus.COMPLETED);
		session = sessionRepository.save(session);
		
		Review review = new Review();
		review.setSession(session);
		review.setReviewer(learner);
		review.setRating(rating);
		review.setComment("Clear explanations and very patient.");
		reviewRepository.save(review);
	}

	private User createUser(String name, String email, String password, Role role) {

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(password));
		user.setRole(role);
		return userRepository.save(user);
	}

	private Skill skill(String name) {

		return skillRepository.findByNameIgnoreCase(name).orElseGet(() -> {
			Skill skill = new Skill();
			skill.setName(name);
			return skillRepository.save(skill);
		});
	}

	private AvailabilitySlot createSlot(User mentor, LocalDateTime start, boolean booked) {
		AvailabilitySlot slot = new AvailabilitySlot();
		slot.setMentor(mentor);
		slot.setStartTime(start);
		slot.setEndTime(start.plusHours(1));
		slot.setBooked(booked);
		return slotRepository.save(slot);
	}
}