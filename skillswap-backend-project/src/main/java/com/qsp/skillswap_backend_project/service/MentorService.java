package com.qsp.skillswap_backend_project.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qsp.skillswap_backend_project.dto.MentorDetail;
import com.qsp.skillswap_backend_project.dto.MentorSummary;
import com.qsp.skillswap_backend_project.dto.PageResponse;
import com.qsp.skillswap_backend_project.dto.SlotRequest;
import com.qsp.skillswap_backend_project.dto.SlotResponse;
import com.qsp.skillswap_backend_project.entity.AvailabilitySlot;
import com.qsp.skillswap_backend_project.entity.User;
import com.qsp.skillswap_backend_project.enums.Role;
import com.qsp.skillswap_backend_project.exception.ResourceNotFoundException;
import com.qsp.skillswap_backend_project.repository.AvailabilitySlotRepository;
import com.qsp.skillswap_backend_project.repository.ReviewRepository;
import com.qsp.skillswap_backend_project.repository.UserRepository;
import com.qsp.skillswap_backend_project.repository.UserSkillRepository;

import lombok.RequiredArgsConstructor;

@Service
public class MentorService {

	private static final int MAX_PAGE_SIZE = 20;
	private static final long MIN_SLOT_MINUTES = 15;
	private static final long MAX_SLOT_MINUTES = 180;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserSkillRepository userSkillRepository;
	@Autowired
	private ReviewRepository reviewRepository;
	@Autowired
	private AvailabilitySlotRepository slotRepository;

	@Transactional(readOnly = true)
	public PageResponse<MentorSummary> search(String skill, double minRating, boolean onlyAvailable, int page,
			int size) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
				Sort.by("name"));

		String skillFilter = skill == null ? "" : skill.trim();
		Page<User> result = userRepository.searchMentors(skillFilter, onlyAvailable, minRating, LocalDateTime.now(),
				pageable);

		return new PageResponse<>(toSummaries(result.getContent()), result.getNumber(), result.getSize(),
				result.getTotalElements(), result.getTotalPages());
	}

	@Transactional(readOnly = true)
	public MentorDetail getMentor(Long id) {
		User mentor = userRepository.findById(id).filter(u -> u.getRole() == Role.MENTOR)
				.orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

		List<SlotResponse> openSlots = slotRepository
				.findByMentorIdAndBookedFalseAndStartTimeAfterOrderByStartTime(id, LocalDateTime.now()).stream()
				.map(this::toSlot).toList();

		return new MentorDetail(toSummaries(List.of(mentor)).get(0), openSlots);
	}

	@Transactional
	public SlotResponse createSlot(String email, SlotRequest request) {
		User mentor = findUser(email);
		LocalDateTime start = request.startTime();
		LocalDateTime end = request.endTime();

		if (!end.isAfter(start)) {
			throw new IllegalArgumentException("End time must be after start time");
		}
		long minutes = Duration.between(start, end).toMinutes();
		if (minutes < MIN_SLOT_MINUTES || minutes > MAX_SLOT_MINUTES) {
			throw new IllegalArgumentException(
					"A slot must be between " + MIN_SLOT_MINUTES + " and " + MAX_SLOT_MINUTES + " minutes long");
		}
		if (slotRepository.existsByMentorIdAndStartTimeLessThanAndEndTimeGreaterThan(mentor.getId(), end, start)) {
			throw new IllegalStateException("This overlaps with another slot of yours");
		}

		AvailabilitySlot slot = new AvailabilitySlot();
		slot.setMentor(mentor);
		slot.setStartTime(start);
		slot.setEndTime(end);
		return toSlot(slotRepository.save(slot));
	}

	@Transactional(readOnly = true)
	public List<SlotResponse> getMySlots(String email) {
		User mentor = findUser(email);
		return slotRepository.findByMentorIdAndEndTimeAfterOrderByStartTime(mentor.getId(), LocalDateTime.now())
				.stream().map(this::toSlot).toList();
	}

	@Transactional
	public void deleteSlot(String email, Long slotId) {
		User mentor = findUser(email);

		// Same message for "does not exist" and "belongs to someone else"
		AvailabilitySlot slot = slotRepository.findById(slotId)
				.filter(s -> s.getMentor().getId().equals(mentor.getId()))
				.orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

		if (slot.isBooked()) {
			throw new IllegalStateException("This slot is already booked and cannot be deleted");
		}
		slotRepository.delete(slot);
	}

	private List<MentorSummary> toSummaries(List<User> mentors) {
		if (mentors.isEmpty()) {
			return List.of();
		}
		List<Long> ids = mentors.stream().map(User::getId).toList();
		LocalDateTime now = LocalDateTime.now();

		Map<Long, List<String>> skillsByMentor = userSkillRepository.findTeachSkillsByUserIds(ids).stream()
				.collect(Collectors.groupingBy(us -> us.getUser().getId(),
						Collectors.mapping(us -> us.getSkill().getName(), Collectors.toList())));

		Map<Long, ReviewRepository.MentorRatingStats> ratings = reviewRepository.ratingStatsForMentors(ids).stream()
				.collect(Collectors.toMap(ReviewRepository.MentorRatingStats::getMentorId, Function.identity()));

		Map<Long, Long> openSlots = slotRepository.countOpenSlots(ids, now).stream()
				.collect(Collectors.toMap(AvailabilitySlotRepository.OpenSlotCount::getMentorId,
						AvailabilitySlotRepository.OpenSlotCount::getOpenSlots));

		return mentors.stream().map(m -> {
			ReviewRepository.MentorRatingStats stats = ratings.get(m.getId());
			Double average = stats == null ? null : Math.round(stats.getAverageRating() * 10) / 10.0;
			long reviewCount = stats == null ? 0 : stats.getReviewCount();
			List<String> skills = skillsByMentor.getOrDefault(m.getId(), List.of()).stream().sorted().toList();

			return new MentorSummary(m.getId(), m.getName(), skills, average, reviewCount,
					openSlots.getOrDefault(m.getId(), 0L));
		}).toList();
	}

	private SlotResponse toSlot(AvailabilitySlot s) {
		return new SlotResponse(s.getId(), s.getStartTime(), s.getEndTime(), s.isBooked());
	}

	private User findUser(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

}
