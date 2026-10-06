package com.qsp.skillswap_backend_project.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qsp.skillswap_backend_project.dto.ProfileResponse;
import com.qsp.skillswap_backend_project.dto.SkillRequest;
import com.qsp.skillswap_backend_project.dto.SkillResponse;
import com.qsp.skillswap_backend_project.dto.UpdateProfileRequest;
import com.qsp.skillswap_backend_project.dto.UserSkillResponse;
import com.qsp.skillswap_backend_project.dto.UserSummary;
import com.qsp.skillswap_backend_project.entity.Skill;
import com.qsp.skillswap_backend_project.entity.User;
import com.qsp.skillswap_backend_project.entity.UserSkill;
import com.qsp.skillswap_backend_project.enums.SkillType;
import com.qsp.skillswap_backend_project.exception.ResourceNotFoundException;
import com.qsp.skillswap_backend_project.repository.SkillRepository;
import com.qsp.skillswap_backend_project.repository.UserRepository;
import com.qsp.skillswap_backend_project.repository.UserSkillRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private SkillRepository skillRepository;
	@Autowired
	private UserSkillRepository userSkillRepository;

	@Transactional(readOnly = true)
	public ProfileResponse getProfile(String email) {
		User user = findUser(email);
		return toProfile(user, userSkillRepository.findByUserId(user.getId()));
	}

	@Transactional
	public ProfileResponse updateName(String email, UpdateProfileRequest request) {
		User user = findUser(email);
		user.setName(request.name().trim());
		return toProfile(user, userSkillRepository.findByUserId(user.getId()));
	}

	@Transactional(readOnly = true)
	public List<UserSkillResponse> getMySkills(String email) {
		User user = findUser(email);
		return userSkillRepository.findByUserId(user.getId()).stream().map(this::toResponse).toList();
	}

	@Transactional
	public UserSkillResponse addSkill(String email, SkillRequest request) {
		User user = findUser(email);
		String name = request.skillName().trim();

		// Reuse the skill if it already exists ("react" and "React" are the same),
		// otherwise create it
		Skill skill = skillRepository.findByNameIgnoreCase(name).orElseGet(() -> {
			Skill created = new Skill();
			created.setName(name);
			return skillRepository.save(created);
		});

		if (userSkillRepository.existsByUserIdAndSkillIdAndType(user.getId(), skill.getId(), request.type())) {
			throw new IllegalStateException("You already added " + skill.getName() + " as " + request.type());
		}

		UserSkill userSkill = new UserSkill();
		userSkill.setUser(user);
		userSkill.setSkill(skill);
		userSkill.setType(request.type());
		return toResponse(userSkillRepository.save(userSkill));
	}

	@Transactional
	public void removeSkill(String email, Long userSkillId) {
		User user = findUser(email);

		// Same message whether the row does not exist or belongs to someone else, so
		// ids cannot be probed
		UserSkill userSkill = userSkillRepository.findById(userSkillId)
				.filter(us -> us.getUser().getId().equals(user.getId()))
				.orElseThrow(() -> new ResourceNotFoundException("Skill entry not found"));

		userSkillRepository.delete(userSkill);
	}

	@Transactional(readOnly = true)
	public List<SkillResponse> getAllSkills() {
		return skillRepository.findAll(Sort.by("name")).stream().map(s -> new SkillResponse(s.getId(), s.getName()))
				.toList();
	}

	// Used by the ADMIN-only endpoint
	@Transactional(readOnly = true)
	public List<UserSummary> listAllUsers() {
		return userRepository.findAll().stream()
				.map(u -> new UserSummary(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getCreditBalance()))
				.toList();
	}

	private User findUser(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private UserSkillResponse toResponse(UserSkill us) {
		return new UserSkillResponse(us.getId(), us.getSkill().getId(), us.getSkill().getName(), us.getType());
	}

	private ProfileResponse toProfile(User user, List<UserSkill> skills) {
		List<String> teach = skills.stream().filter(us -> us.getType() == SkillType.TEACH)
				.map(us -> us.getSkill().getName()).toList();
		List<String> learn = skills.stream().filter(us -> us.getType() == SkillType.LEARN)
				.map(us -> us.getSkill().getName()).toList();
		return new ProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(),
				user.getCreditBalance(), teach, learn);
	}
}
