package com.qsp.skillswap_backend_project.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qsp.skillswap_backend_project.entity.User;
import com.qsp.skillswap_backend_project.enums.Role;

import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);
	
	long countByRole(Role role);

    @Query("select coalesce(sum(u.creditBalance), 0L) from User u")
    long totalCredits();

	@Query("""
			select u from User u
			where u.role = com.qsp.skillswap_backend_project.enums.Role.MENTOR
			  and (:skill = '' or exists (
			        select 1 from UserSkill us
			        where us.user = u
			          and us.type = com.qsp.skillswap_backend_project.enums.SkillType.TEACH
			          and lower(us.skill.name) like lower(concat('%', :skill, '%'))))
			  and (:onlyAvailable = false or exists (
			        select 1 from AvailabilitySlot s
			        where s.mentor = u and s.booked = false and s.startTime > :now))
			  and (:minRating <= 0.0 or (
			        select avg(r.rating) from Review r
			        where r.session.mentor = u and r.reviewer = r.session.learner) >= :minRating)
			""")
	Page<User> searchMentors(@Param("skill") String skill, @Param("onlyAvailable") boolean onlyAvailable,
			@Param("minRating") double minRating, @Param("now") LocalDateTime now, Pageable pageable);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT u FROM User u WHERE u.id = :id")
	Optional<User> findByIdForUpdate(@Param("id") Long id);

}
