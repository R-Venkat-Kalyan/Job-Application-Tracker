package com.example.Job_Application_Tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.Job_Application_Tracker.entity.RegisterUsers;
import com.example.Job_Application_Tracker.entity.service.RegisterUsersService;

@Controller
public class RouteController {

	@Autowired
	private RegisterUsersService registerUsersService;
	
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
	
	@PostMapping("/register")
	public String registerNewUser(@ModelAttribute RegisterUsers user) {
		registerUsersService.registerUser(user);
		return "redirect:/sign-in";
	}
	
	
}
