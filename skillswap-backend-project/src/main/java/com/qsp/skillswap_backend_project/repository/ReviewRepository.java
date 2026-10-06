package com.qsp.skillswap_backend_project.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.qsp.skillswap_backend_project.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	boolean existsBySessionIdAndReviewerId(Long sessionId, Long reviewerId);

	List<Review> findBySessionMentorId(Long mentorId);
	
	@Query("select r.session.id from Review r where r.reviewer.id = :reviewerId")
    List<Long> findSessionIdsReviewedBy(@Param("reviewerId") Long reviewerId);

	@Query("""
			    select avg(r.rating)
			    from Review r
			    where r.session.mentor.id = :mentorId
			""")
	Double averageRatingForMentor(@Param("mentorId") Long mentorId);

	@Query("""
			select r.session.mentor.id as mentorId, avg(r.rating) as averageRating, count(r) as reviewCount
			from Review r
			where r.session.mentor.id in :mentorIds and r.reviewer.id = r.session.learner.id
			group by r.session.mentor.id
			""")
	List<MentorRatingStats> ratingStatsForMentors(@Param("mentorIds") Collection<Long> mentorIds);

	@Query("""
            select r from Review r
            join fetch r.reviewer
            join r.session s
            where s.mentor.id = :mentorId and r.reviewer.id = s.learner.id
            order by r.id desc
            """)
    List<Review> findMentorReviews(@Param("mentorId") Long mentorId, Pageable pageable);
	
	@Query("""
            select r.session.mentor.id as mentorId, avg(r.rating) as averageRating, count(r) as reviewCount
            from Review r
            where r.reviewer.id = r.session.learner.id
            group by r.session.mentor.id
            order by avg(r.rating) desc, count(r) desc
            """)
    List<MentorRatingStats> topMentorStats(Pageable pageable);
	
	@Query("select avg(r.rating) from Review r where r.reviewer.id = r.session.learner.id")
    Double overallAverageRating();
	
	@Query("""
            select r from Review r
            join fetch r.reviewer
            join fetch r.session s
            join fetch s.mentor
            join fetch s.learner
            order by r.id desc
            """)
    List<Review> findRecentWithDetails(Pageable pageable);
	
	interface MentorRatingStats {
		Long getMentorId();

		Double getAverageRating();

		Long getReviewCount();
	}

}