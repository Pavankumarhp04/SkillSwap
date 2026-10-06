package com.qsp.skillswap_backend_project.entity;

import com.qsp.skillswap_backend_project.enums.SessionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
public class LearningSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "mentor_id")
	private User mentor;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "learner_id")
	private User learner;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "slot_id", unique = true)
	private AvailabilitySlot slot;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SessionStatus status = SessionStatus.BOOKED;

	@Column(name = "meeting_link")
	private String meetingLink;

}
