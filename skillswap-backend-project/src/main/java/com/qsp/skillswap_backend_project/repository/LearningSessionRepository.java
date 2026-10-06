package com.qsp.skillswap_backend_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qsp.skillswap_backend_project.entity.LearningSession;
import com.qsp.skillswap_backend_project.enums.SessionStatus;

import jakarta.persistence.LockModeType;

public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {

	List<LearningSession> findByMentorIdOrLearnerId(Long mentorId, Long learnerId);

	long countByStatus(SessionStatus status);

	@Query("""
			select s from LearningSession s
			join fetch s.mentor
			join fetch s.learner
			join fetch s.slot sl
			where s.mentor.id = :userId or s.learner.id = :userId
			order by sl.startTime desc
			""")
	List<LearningSession> findAllForUser(@Param("userId") Long userId);

	@Query("""
			select s from LearningSession s
			join fetch s.mentor
			join fetch s.learner
			join fetch s.slot sl
			order by sl.startTime desc
			""")
	List<LearningSession> findRecent(Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from LearningSession s where s.id = :id")
	Optional<LearningSession> findByIdForUpdate(@Param("id") Long id);

}