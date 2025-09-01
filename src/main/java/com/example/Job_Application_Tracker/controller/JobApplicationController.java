package com.example.Job_Application_Tracker.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;


@Controller
public class JobApplicationController {

	@Autowired
    private JobApplicationRepository jobRepo;

    @Value("${github.token}")
    private String githubToken;

    @Value("${github.repo.owner}")
    private String githubOwner;

    @Value("${github.repo.name}")
    private String githubRepo;

    private static final DateTimeFormatter  DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");;

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
                                     @RequestParam(required = false) String applicationLink,
                                     @RequestParam(required = false) Double expectedCtc,
                                     @RequestParam String dateApplied,
                                     @RequestParam(required = false) String jobDescription,
                                     @RequestParam MultipartFile resumeUsed,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) throws IOException {

        if (session != null && session.getAttribute("email") != null) {
            String email = (String) session.getAttribute("email");

            // Upload resume to GitHub if present
            String resumeUrl = null;
            if (!resumeUsed.isEmpty()) {
                resumeUrl = uploadResumeToGitHub(resumeUsed, email);
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
            job.setEmail(email);
            job.setStatus("Applied");
            job.setResumeUrl(resumeUrl);

            jobRepo.save(job);
            redirectAttributes.addFlashAttribute("toastMessage", "Job successfully added!");
            return "redirect:/applied-jobs";
        }
        redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
        session.invalidate();
        return "redirect:/sign-in";
    }

    private String uploadResumeToGitHub(MultipartFile file, String email) throws IOException {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        // Generate human-readable timestamp
        String timestamp = DATE_TIME_FORMATTER.format(LocalDateTime.now());

        // Use email + date + time format
        String uniqueFileName = email + "-" + timestamp + extension;

        String encodedFileName = URLEncoder.encode(uniqueFileName, StandardCharsets.UTF_8);
        String githubApiUrl = "https://api.github.com/repos/" + githubOwner + "/" + githubRepo + "/contents/" + encodedFileName;

        String base64Content = Base64.getEncoder().encodeToString(file.getBytes());

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Job Application Resume Upload - " + uniqueFileName);
        body.put("content", base64Content);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(githubToken);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(githubApiUrl, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return "https://github.com/" + githubOwner + "/" + githubRepo + "/blob/main/" + uniqueFileName;
        } else {
            throw new IOException("Failed to upload resume to GitHub");
        }
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

    /** Download resume from GitHub even if repo is private **/
    @GetMapping("/resume/{id}")
    public ResponseEntity<byte[]> downloadResume(@PathVariable String id) throws IOException {
        Optional<JobApplication> jobOptional = jobRepo.findById(id);
        if (jobOptional.isEmpty() || jobOptional.get().getResumeUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        String resumeUrl = jobOptional.get().getResumeUrl();
        String fileName = resumeUrl.substring(resumeUrl.lastIndexOf("/") + 1);

        // GitHub API URL to fetch file content
        String githubApiUrl = "https://api.github.com/repos/" + githubOwner + "/" + githubRepo + "/contents/" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(githubApiUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String base64Content = ((String) response.getBody().get("content")).replaceAll("\\s+", "");
            byte[] fileBytes = Base64.getDecoder().decode(base64Content);

            String contentType = fileName.endsWith(".pdf") ? "application/pdf"
                    : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileBytes);
        }
        return ResponseEntity.notFound().build();
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
