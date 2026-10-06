package com.qsp.skillswap_backend_project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qsp.skillswap_backend_project.entity.Skill;

public interface SkillRepository  extends JpaRepository<Skill, Long> {

    Optional<Skill> findByNameIgnoreCase(String name);

}