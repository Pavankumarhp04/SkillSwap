package com.qsp.skillswap_backend_project.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.qsp.skillswap_backend_project.dto.SlotRequest;
import com.qsp.skillswap_backend_project.dto.SlotResponse;
import com.qsp.skillswap_backend_project.service.MentorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mentors/me/slots")
public class MentorSlotController {

	@Autowired
	private MentorService mentorService;

    @GetMapping
    public List<SlotResponse> mySlots(Principal principal) {
        return mentorService.getMySlots(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlotResponse create(Principal principal, @Valid @RequestBody SlotRequest request) {
        return mentorService.createSlot(principal.getName(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Principal principal, @PathVariable Long id) {
        mentorService.deleteSlot(principal.getName(), id);
    }
}
