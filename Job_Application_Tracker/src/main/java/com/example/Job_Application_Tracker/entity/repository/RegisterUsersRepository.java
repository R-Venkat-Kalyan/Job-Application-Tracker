package com.example.Job_Application_Tracker.entity.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.Job_Application_Tracker.entity.RegisterUsers;

@Repository
public interface RegisterUsersRepository extends MongoRepository<RegisterUsers, String> {

}
