package com.example.Job_Application_Tracker.entity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Job_Application_Tracker.entity.RegisterUsers;
import com.example.Job_Application_Tracker.entity.repository.RegisterUsersRepository;

@Service
public class RegisterUsersService {

	@Autowired
	private RegisterUsersRepository registerUsersRepository;
	
	public void registerUser(RegisterUsers registerUsers) {
		registerUsersRepository.save(registerUsers);
	}
}

