package com.example.Job_Application_Tracker.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Job_Application_Tracker.entity.JobApplication;
import com.example.Job_Application_Tracker.repository.JobApplicationRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class JobApplicationController {

	@Autowired
	private JobApplicationRepository jobRepo;

	@GetMapping("/add-job")
	public String addJob(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
		if (session != null && session.getAttribute("email") != null) {
			model.addAttribute("mainContent", "add-job");
			model.addAttribute("heading", "Add Job");
			return "dashboard-layout";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@PostMapping("/save-job")
	public String saveJobApplication(@RequestParam String companyName, @RequestParam String roleName,
			@RequestParam String roleType, @RequestParam String platformUsed,
			@RequestParam(required = false) String applicationLink, @RequestParam(required = false) Double expectedCtc,
			@RequestParam String dateApplied, @RequestParam(required = false) String jobDescription,
			@RequestParam MultipartFile resumeUsed, HttpSession session, RedirectAttributes redirectAttributes)
			throws IOException {
		if (session != null && session.getAttribute("email") != null) {

			String resumeName = null;
			String resumeType = null;
			byte[] resumeData = null;

			if (!resumeUsed.isEmpty()) {
				resumeName = StringUtils.cleanPath(resumeUsed.getOriginalFilename());
				resumeType = resumeUsed.getContentType();
				resumeData = resumeUsed.getBytes();
			}

			JobApplication job = new JobApplication();
			job.setCompanyName(companyName);
			job.setRoleName(roleName);
			job.setRoleType(roleType);
			job.setPlatformUsed(platformUsed);
			job.setApplicationLink(applicationLink);
			job.setExpectedCtc(expectedCtc);
			job.setDateApplied(LocalDate.parse(dateApplied));
			job.setJobDescription(jobDescription);
			String email = (String) session.getAttribute("email");
			job.setEmail(email);
			job.setStatus("Applied"); // default status
			job.setResumeName(resumeName);
			job.setResumeType(resumeType);
			job.setResumeData(resumeData);

			jobRepo.save(job);
			redirectAttributes.addFlashAttribute("toastMessage", "Job successfully added!");
			return "redirect:/applied-jobs";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@GetMapping("/applied-jobs")
	public String findUserJobs(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		if (session != null && session.getAttribute("email") != null) {
			String email = (String) session.getAttribute("email");
			Set<JobApplication> userJobs = jobRepo.findByEmail(email);
			model.addAttribute("jobs", userJobs);
			model.addAttribute("heading", "All Applied Jobs");
			model.addAttribute("message", "Here’s a list of all your job applications logged in JobTrackrly.");
			model.addAttribute("mainContent", "all-jobs");
			return "dashboard-layout";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@GetMapping("/resume/{id}")
	public ResponseEntity<byte[]> viewResume(@PathVariable String id) {

		Optional<JobApplication> jobOptional = jobRepo.findById(id);

		if (jobOptional.isPresent()) {
			JobApplication job = jobOptional.get();

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + job.getResumeName() + "\"")
					.contentType(MediaType.parseMediaType(job.getResumeType())).body(job.getResumeData());
		} else {
			return ResponseEntity.notFound().build();
		}

	}

	@GetMapping("/view-application/{id}")
	public String viewApplicationDetails(@PathVariable("id") String id, Model model,
			RedirectAttributes redirectAttributes, HttpSession session) {
		if (session != null && session.getAttribute("email") != null) {
			Optional<JobApplication> jobOpt = jobRepo.findById(id);

			if (jobOpt.isPresent()) {
				model.addAttribute("job", jobOpt.get());
				model.addAttribute("mainContent", "view-job");
				return "dashboard-layout";
			} else {
				// Optional: redirect to error or job list page if not found
				return "redirect:/applied-jobs";
			}
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@GetMapping("/shortlist/{id}")
	public String shortlistJob(@PathVariable String id, RedirectAttributes redirectAttributes, HttpSession session) {
		if (session != null && session.getAttribute("email") != null) {
			jobRepo.findById(id).ifPresent(job -> {
				job.setStatus("Shortlisted");
				jobRepo.save(job);
			});
			redirectAttributes.addFlashAttribute("toastMessage", "Job marked as Shortlisted!");
			return "redirect:/applied-jobs";
		}

		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	// ✅ Reject a job
	@GetMapping("/reject/{id}")
	public String rejectJob(@PathVariable String id, RedirectAttributes redirectAttributes, HttpSession session) {
		if (session != null && session.getAttribute("email") != null) {
			jobRepo.findById(id).ifPresent(job -> {
				job.setStatus("Rejected");
				jobRepo.save(job);
			});
			redirectAttributes.addFlashAttribute("toastMessage", "Job marked as Rejected!");
			return "redirect:/applied-jobs";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	// ✅ DELETE a job
	@GetMapping("/delete-job/{id}")
	public String deleteJob(@PathVariable String id, RedirectAttributes redirectAttributes, HttpSession session) {
		if (session != null && session.getAttribute("email") != null) {
			jobRepo.deleteById(id);
			redirectAttributes.addFlashAttribute("toastMessage", "Job deleted successfully!");
			return "redirect:/applied-jobs";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@GetMapping("/shortlisted")
	public String viewShortlistedJobs(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		if (session != null && session.getAttribute("email") != null) {
			String email = (String) session.getAttribute("email");
			Set<JobApplication> shortlistedJobs = jobRepo.findByEmailAndStatus(email, "Shortlisted");
			model.addAttribute("jobs", shortlistedJobs);
			model.addAttribute("heading", "Shortlisted Jobs");
			model.addAttribute("message", "Nice! You’ve impressed someone. Keep pushing forward!");
			model.addAttribute("mainContent", "all-jobs");
			return "dashboard-layout";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@GetMapping("/rejected")
	public String viewRejectedJobs(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		if (session != null && session.getAttribute("email") != null) {
			String email = (String) session.getAttribute("email");
			Set<JobApplication> rejectedJobs = jobRepo.findByEmailAndStatus(email, "Rejected");
			model.addAttribute("jobs", rejectedJobs);
			model.addAttribute("heading", "Rejected Jobs");
			model.addAttribute("message", "Chin up! Even legends get rejected. Time to bounce back harder.");
			model.addAttribute("mainContent", "all-jobs");
			return "dashboard-layout";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@GetMapping("/no-update")
	public String viewNoUpdateJobs(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		if (session != null && session.getAttribute("email") != null) {
			String email = (String) session.getAttribute("email");
			Set<JobApplication> noUpdateJobs = jobRepo.findByEmailAndStatus(email, "Applied");
			model.addAttribute("jobs", noUpdateJobs);
			model.addAttribute("heading", "No Update Jobs");
			model.addAttribute("message", "Still waiting? Maybe they're thinking. Keep applying, don’t stop.");
			model.addAttribute("mainContent", "all-jobs");
			return "dashboard-layout";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

}
