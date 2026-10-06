package com.qsp.skillswap_backend_project.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qsp.skillswap_backend_project.dto.WalletResponse;
import com.qsp.skillswap_backend_project.service.SessionService;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

	@Autowired
	private SessionService sessionService;

	// Current balance plus the last 50 credit transactions, newest first
	@GetMapping
	public WalletResponse wallet(Principal principal) {
		return sessionService.wallet(principal.getName());
	}
}
