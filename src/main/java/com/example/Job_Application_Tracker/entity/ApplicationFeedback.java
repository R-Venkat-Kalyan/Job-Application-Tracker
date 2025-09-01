package com.example.Job_Application_Tracker.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "User_Feedbacks")
public class ApplicationFeedback {
	
	@Id
	private String id;
	
	private String category;     // e.g., suggestion, issue, improvement, other
    private String page;         // e.g., dashboard, add-job, job-list, profile, other
    private String message;      // actual feedback message
    private String email;        // optional, can be set from session
    private boolean notified;
	
    
    public ApplicationFeedback() {
    	
    }


	public ApplicationFeedback(String id, String category, String page, String message, String email,
			boolean notified) {
		super();
		this.id = id;
		this.category = category;
		this.page = page;
		this.message = message;
		this.email = email;
		this.notified = notified;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}


	public String getPage() {
		return page;
	}


	public void setPage(String page) {
		this.page = page;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public boolean isNotified() {
		return notified;
	}


	public void setNotified(boolean notified) {
		this.notified = notified;
	}
    
    

}


