
package com.example.Job_Application_Tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobApplicationTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobApplicationTrackerApplication.class, args);
	}

} 

