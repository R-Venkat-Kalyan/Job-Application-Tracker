package com.example.Job_Application_Tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RouteController {

	@GetMapping("")
	public String home() {
		return "index";
	}
	
	@GetMapping("/sign-in")
	public String signIn() {
		return "sign-in";
	}
	
	@GetMapping("/sign-up")
	public String signUp() {
		return "sign-up";
	}
	
	@GetMapping("/terms-privacy")
	public String termsAndPrivacy() {
		return "terms";
	}
}
