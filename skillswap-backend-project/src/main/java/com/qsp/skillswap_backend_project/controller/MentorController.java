package com.qsp.skillswap_backend_project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qsp.skillswap_backend_project.dto.MentorDetail;
import com.qsp.skillswap_backend_project.dto.MentorSummary;
import com.qsp.skillswap_backend_project.dto.PageResponse;
import com.qsp.skillswap_backend_project.dto.ReviewResponse;
import com.qsp.skillswap_backend_project.service.MentorService;
import com.qsp.skillswap_backend_project.service.ReviewService;

@RestController
@RequestMapping("/api/mentors")
public class MentorController {
	
	@Autowired
	private MentorService mentorService;
	@Autowired
	private ReviewService reviewService;
	
	@GetMapping
    public PageResponse<MentorSummary> search(
            @RequestParam(defaultValue = "") String skill,
            @RequestParam(defaultValue = "0") double minRating,
            @RequestParam(defaultValue = "false") boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        return mentorService.search(skill, minRating, available, page, size);
    }

    @GetMapping("/{id}")
    public MentorDetail getMentor(@PathVariable Long id) {
        return mentorService.getMentor(id);
    }
    
    @GetMapping("/{id}/reviews")
    public List<ReviewResponse> reviews(@PathVariable Long id) {
        return reviewService.mentorReviews(id);
    }
}
