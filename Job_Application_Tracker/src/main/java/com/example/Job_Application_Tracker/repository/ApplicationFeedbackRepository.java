package com.example.Job_Application_Tracker.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.Job_Application_Tracker.entity.ApplicationFeedback;

public interface ApplicationFeedbackRepository extends MongoRepository<ApplicationFeedback, String> {

}
