package com.qsp.skillswap_backend_project.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qsp.skillswap_backend_project.entity.UserSkill;
import com.qsp.skillswap_backend_project.enums.SkillType;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

	List<UserSkill> findByUserId(Long userId);

	List<UserSkill> findBySkillIdAndType(Long skillId, SkillType type);

	boolean existsByUserIdAndSkillIdAndType(Long userId, Long skillId, SkillType type);

	@Query("""
			select us from UserSkill us join fetch us.skill
			where us.user.id in :userIds and us.type = com.qsp.skillswap_backend_project.enums.SkillType.TEACH
			""")
	List<UserSkill> findTeachSkillsByUserIds(@Param("userIds") Collection<Long> userIds);
}