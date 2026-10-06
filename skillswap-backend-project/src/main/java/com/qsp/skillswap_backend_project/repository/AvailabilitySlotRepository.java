package com.qsp.skillswap_backend_project.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qsp.skillswap_backend_project.entity.AvailabilitySlot;

import jakarta.persistence.LockModeType;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

	List<AvailabilitySlot> findByMentorIdAndBookedFalseAndStartTimeAfterOrderByStartTime(Long mentorId,
			LocalDateTime after);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from AvailabilitySlot s where s.id = :id")
	Optional<AvailabilitySlot> findByIdForUpdate(@Param("id") Long id);

	List<AvailabilitySlot> findByMentorIdAndEndTimeAfterOrderByStartTime(Long mentorId, LocalDateTime after);

	boolean existsByMentorIdAndStartTimeLessThanAndEndTimeGreaterThan(Long mentorId, LocalDateTime newEnd,
			LocalDateTime newStart);

	long countByBookedFalseAndStartTimeAfter(LocalDateTime after);

	@Query("""
			select s.mentor.id as mentorId, count(s) as openSlots
			from AvailabilitySlot s
			where s.mentor.id in :mentorIds and s.booked = false and s.startTime > :now
			group by s.mentor.id
			""")
	List<OpenSlotCount> countOpenSlots(@Param("mentorIds") Collection<Long> mentorIds, @Param("now") LocalDateTime now);

	interface OpenSlotCount {
		Long getMentorId();

		Long getOpenSlots();
	}
}