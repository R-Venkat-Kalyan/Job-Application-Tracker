package com.example.Job_Application_Tracker.controller;

import java.nio.file.Files;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.Job_Application_Tracker.entity.ApplicationFeedback;
import com.example.Job_Application_Tracker.entity.ContactMessage;
import com.example.Job_Application_Tracker.entity.RegisterUsers;
import com.example.Job_Application_Tracker.repository.ApplicationFeedbackRepository;
import com.example.Job_Application_Tracker.repository.ContactMessageRepository;
import com.example.Job_Application_Tracker.repository.JobApplicationRepository;
import com.example.Job_Application_Tracker.service.RegisterUsersService;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class RouteController {

	@Autowired
	private RegisterUsersService registerUsersService;

	@Autowired
	private ApplicationFeedbackRepository feedbackRepositor;

	@Autowired
	private JobApplicationRepository jobRepo;

	@Autowired
	private ContactMessageRepository contactRepo;

	@Autowired
	private JavaMailSender mailSender;

	@GetMapping("")
	public String home() {
		return "index";
	}

	@GetMapping("/sign-in")
	public String signIn() {
		return "sign-in";
	}

	@GetMapping("/sign-up")
	public String signUp() {
		return "sign-up";
	}

	@GetMapping("/terms-privacy")
	public String termsAndPrivacy() {
		return "terms";
	}

	@PostMapping("/register")
	public String registerNewUser(@ModelAttribute RegisterUsers user, RedirectAttributes redirectAttributes,
			HttpSession session) {
		String regEmail = user.getEmail();
		if(registerUsersService.isUserExists(regEmail)) {
			redirectAttributes.addFlashAttribute("toastMessage", "User Already Exists ✔ Please Sign-In !!");
			return "redirect:/sign-in";
		}
		// Save user details in session
		session.setAttribute("user", user);
		// Generate OTP and send email
		sendOTP(regEmail, session);
		return "redirect:/otp";
	}
	
	@GetMapping("/verify")
	public String verifyUser(@RequestParam("userOTP") int userOTP, HttpSession session,
			RedirectAttributes redirectAttributes) {
		int generatedOTP = (int) session.getAttribute("generatedOTP");
//	    System.out.println(userOTP+" "+generatedOTP);
		if (userOTP == generatedOTP) {
			RegisterUsers user = (RegisterUsers) session.getAttribute("user");
			registerUsersService.registerUser(user);
			session.invalidate(); // Clear session after successful registration
			redirectAttributes.addFlashAttribute("toastMessage", "Registration Done..✔ \nPlease Sign-In Now !!");
			return "redirect:/sign-in";
		} else {
			redirectAttributes.addFlashAttribute("toastMessage", "Invalid OTP..❌❌");
			return "redirect:/otp"; // Redirect back to registration if OTP doesn't match
		}
	}

	private void sendOTP(String regEmail, HttpSession session) {
		// Code to send OTP via email using mailsender
		String from = "taskprompter@gmail.com";
		String to = regEmail;
		int generatedOTP = generateRandomOTP();
		session.setAttribute("generatedOTP", generatedOTP);
		try {
			// Read HTML email template
			Resource resource = new ClassPathResource("templates/otp-mail.html");
			String emailTemplate = new String(Files.readAllBytes(resource.getFile().toPath()));

			// Replace placeholder with generated OTP
			String emailBody = emailTemplate.replace("{{otp}}", String.valueOf(generatedOTP));

			// Create MimeMessage
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject("JobTrackrly.com: Verify email address for JobTrackrly");
			helper.setText(emailBody, true);

			// Send email
			mailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exception
		}
	}

	private int generateRandomOTP() {
		Random random = new Random();
		return 100000 + random.nextInt(900000);
	}

	@GetMapping("/otp")
	public String otpForm() {
		return "otp-form";
	}

	@GetMapping("/userLogin")
	public String dashboard(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model,
			HttpSession session) {
		List<RegisterUsers> allUsers = registerUsersService.allUsers();
		boolean userFound = false;
		for (RegisterUsers user : allUsers) {
			String pwd = request.getParameter("password");
			String email = request.getParameter("email");
			if (user.getEmail().equals(email) && user.getPassword().equals(pwd)) {
				// Clear any OAuth session data if present
				session.removeAttribute("oauthUser");

				session.setAttribute("email", email);
				session.setAttribute("userName", user.getFullName());
				session.setMaxInactiveInterval(30 * 60);
				String mail = (String) session.getAttribute("email");

				long total = jobRepo.countByEmail(mail);
				long shortlisted = jobRepo.countByEmailAndStatus(mail, "Shortlisted");
				long rejected = jobRepo.countByEmailAndStatus(mail, "Rejected");
				long noUpdate = jobRepo.countByEmailAndStatus(mail, "Applied"); // 'applied' = no update

				model.addAttribute("totalJobs", total);
				model.addAttribute("shortlistedJobs", shortlisted);
				model.addAttribute("rejectedJobs", rejected);
				model.addAttribute("noUpdateJobs", noUpdate);
				model.addAttribute("userName", user.getFullName());
				model.addAttribute("userMail", user.getEmail());
				userFound = true;
				return "redirect:/dashboard";
			} else if (user.getEmail().equals(email) && !user.getPassword().equals(pwd)) {
				redirectAttributes.addFlashAttribute("toastMessage", "Invalid Password..❌❌");
				session.invalidate();
				return "redirect:/sign-in";
			}
		}

		if (!userFound) {
			redirectAttributes.addFlashAttribute("toastMessage", "User Not Found..❌\nPlease Register First");
			session.invalidate();
			return "redirect:/sign-up";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Invalid Credentials..❌❌");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@GetMapping("/dashboard")
	public String getDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		if (session != null && session.getAttribute("email") != null) {
			String email = (String) session.getAttribute("email");
			String userName = (String) session.getAttribute("userName");
			model.addAttribute("userName", userName);
			model.addAttribute("userMail", email);

			long total = jobRepo.countByEmail(email);
			long shortlisted = jobRepo.countByEmailAndStatus(email, "Shortlisted");
			long rejected = jobRepo.countByEmailAndStatus(email, "Rejected");
			long noUpdate = jobRepo.countByEmailAndStatus(email, "Applied"); // 'applied' = no update

			model.addAttribute("totalJobs", total);
			model.addAttribute("shortlistedJobs", shortlisted);
			model.addAttribute("rejectedJobs", rejected);
			model.addAttribute("noUpdateJobs", noUpdate);
			return "dashboard";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@GetMapping("/update-profile")
	public String updateProfile(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		if (session != null && session.getAttribute("email") != null) {
			String email = (String) session.getAttribute("email");
			RegisterUsers user = registerUsersService.findUserByMail(email);
			model.addAttribute("user", user);
			model.addAttribute("mainContent", "update-profile");
			model.addAttribute("heading", "Update Profile");
			return "dashboard-layout";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@PostMapping("/update")
	public String saveUpdatedProfile(@ModelAttribute RegisterUsers user, HttpSession session,
			RedirectAttributes redirectAttributes) {
		if (session != null && session.getAttribute("email") != null) {
			String email = (String) session.getAttribute("email");
			user.setEmail(email);
			registerUsersService.registerUser(user);
			session.invalidate();
			return "redirect:/sign-in";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@GetMapping("/feedback")
	public String feedack(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
		if (session != null && session.getAttribute("email") != null) {
			model.addAttribute("mainContent", "feedback-form");
			model.addAttribute("heading", "Feedack");
			return "dashboard-layout";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@PostMapping("/submit-feedback")
	public String submitFeedback(@ModelAttribute ApplicationFeedback feedback, HttpSession session,
			RedirectAttributes redirectAttributes) {
		if (session != null && session.getAttribute("email") != null) {
			String email = (String) session.getAttribute("email");
			feedback.setEmail(email);
			feedback.setNotified(false);
			feedbackRepositor.save(feedback);
			redirectAttributes.addFlashAttribute("toastMessage", "Feedback Submitted !!");
			return "redirect:/dashboard";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Access Denied..❌\nPlease Sign-In First");
		session.invalidate();
		return "redirect:/sign-in";
	}

	@PostMapping("/submit-contact")
	public String submitContactForm(@ModelAttribute ContactMessage contact, RedirectAttributes redirectAttributes) {
		contactRepo.save(contact);
		redirectAttributes.addFlashAttribute("successMessage", "Feedback submitted successfully!");

		return "redirect:/#contact";
	}

	@GetMapping("/log-out")
	public String logOut(HttpSession session) {
		session.invalidate();
		return "index";
	}
}
