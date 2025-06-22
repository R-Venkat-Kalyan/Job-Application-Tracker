package com.example.Job_Application_Tracker.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.Job_Application_Tracker.entity.JobApplication;

public interface JobApplicationRepository extends MongoRepository<JobApplication, String> {
	
	Set<JobApplication> findByEmail(String email);
	
	Set<JobApplication> findByEmailAndStatus(String email, String status);

	long countByEmail(String email);

    long countByEmailAndStatus(String email, String status);
	
}
