package com.example.Job_Application_Tracker.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.example.Job_Application_Tracker.entity.JobApplication;

public interface JobApplicationRepository extends MongoRepository<JobApplication, String> {
	
	Set<JobApplication> findByEmail(String email);
	
	Set<JobApplication> findByEmailAndStatus(String email, String status);

	long countByEmail(String email);

    long countByEmailAndStatus(String email, String status);
    
    @Query("{ 'email': ?0, 'status': 'NO_UPDATE', 'dateApplied': { $lte: ?1 } }")
    List<JobApplication> findNoUpdateAfterTwoWeeks(String email, LocalDate cutoff);

    long countByEmailAndDateAppliedAfter(String email, LocalDate from);

    long countByEmailAndStatusAndDateAppliedAfter(String email, String status, LocalDate from);

    @Query(value = "{}", fields = "{ 'email' : 1 }")
    List<JobApplication> findAllDistinctEmails(); 

    
	
}
