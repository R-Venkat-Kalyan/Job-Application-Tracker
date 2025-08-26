package com.example.Job_Application_Tracker.entity;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Job_Applications")
public class JobApplication {

	@Id
	private String id;

	private String companyName;
	private String roleName;
	private String roleType;
	private String platformUsed;
	private String applicationLink;
	private Double expectedCtc;
	private LocalDate dateApplied;
	private String jobDescription;
	private String email;
	private String status;

	// Instead of storing resume data in DB
	private String resumeUrl;

	public JobApplication() {

	}

	public JobApplication(String id, String companyName, String roleName, String roleType, String platformUsed,
			String applicationLink, Double expectedCtc, LocalDate dateApplied, String jobDescription, String email,
			String status, String resumeUrl) {
		super();
		this.id = id;
		this.companyName = companyName;
		this.roleName = roleName;
		this.roleType = roleType;
		this.platformUsed = platformUsed;
		this.applicationLink = applicationLink;
		this.expectedCtc = expectedCtc;
		this.dateApplied = dateApplied;
		this.jobDescription = jobDescription;
		this.email = email;
		this.status = status;
		this.resumeUrl = resumeUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleType() {
		return roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

	public String getPlatformUsed() {
		return platformUsed;
	}

	public void setPlatformUsed(String platformUsed) {
		this.platformUsed = platformUsed;
	}

	public String getApplicationLink() {
		return applicationLink;
	}

	public void setApplicationLink(String applicationLink) {
		this.applicationLink = applicationLink;
	}

	public Double getExpectedCtc() {
		return expectedCtc;
	}

	public void setExpectedCtc(Double expectedCtc) {
		this.expectedCtc = expectedCtc;
	}

	public LocalDate getDateApplied() {
		return dateApplied;
	}

	public void setDateApplied(LocalDate dateApplied) {
		this.dateApplied = dateApplied;
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResumeUrl() {
		return resumeUrl;
	}

	public void setResumeUrl(String resumeUrl) {
		this.resumeUrl = resumeUrl;
	}
	
}

	