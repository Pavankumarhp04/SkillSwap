package com.qsp.skillswap_backend_project.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qsp.skillswap_backend_project.entity.CreditTransaction;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

	List<CreditTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

	@Query("""
			select t from CreditTransaction t
			left join fetch t.session s
			left join fetch s.mentor
			left join fetch s.learner
			where t.user.id = :userId
			order by t.createdAt desc
			""")
	List<CreditTransaction> findHistory(@Param("userId") Long userId, Pageable pageable);

}