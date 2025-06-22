package com.example.Job_Application_Tracker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Job_Application_Tracker.entity.RegisterUsers;
import com.example.Job_Application_Tracker.repository.RegisterUserRepository;

@Service
public class RegisterUsersService {
	
	@Autowired
	private RegisterUserRepository registerUserRepository;
	
	public void registerUser(RegisterUsers registerUsers) {
		registerUserRepository.save(registerUsers);
	}
	
	public List<RegisterUsers> allUsers(){
		return registerUserRepository.findAll();
	}

	public RegisterUsers findUserByMail(String email) {
		return registerUserRepository.findById(email).orElse(null);
	}
}
