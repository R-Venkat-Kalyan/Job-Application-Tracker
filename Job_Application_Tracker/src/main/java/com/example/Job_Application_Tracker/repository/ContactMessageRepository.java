package com.example.Job_Application_Tracker.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.Job_Application_Tracker.entity.ContactMessage;

public interface ContactMessageRepository extends MongoRepository<ContactMessage, String> {

}
