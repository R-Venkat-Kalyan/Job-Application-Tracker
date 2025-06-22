package com.example.Job_Application_Tracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

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
	public String registerNewUser(@ModelAttribute RegisterUsers user, RedirectAttributes redirectAttributes) {
		registerUsersService.registerUser(user);
		redirectAttributes.addFlashAttribute("toastMessage", "Registeration Done..❌\nPlease Sign-In Now !!");
		return "redirect:/sign-in";
	}

//	@PostMapping("/dashboard")
//	public String dashboard() {
//		return "dashboard";
//	}

	@PostMapping("/dashboard")
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
				return "dashboard";
			} else if (user.getEmail().equals(email) && !user.getPassword().equals(pwd)) {
				redirectAttributes.addFlashAttribute("toastMessage", "Invalid Password..❌❌");
				return "redirect:/sign-in";
			}
		}

		if (!userFound) {
			redirectAttributes.addFlashAttribute("toastMessage", "User Not Found..❌\nPlease Register First");
			return "redirect:/sign-up";
		}
		redirectAttributes.addFlashAttribute("toastMessage", "Invalid Credentials..❌❌");
		return "redirect:/sign-in";
	}

	@GetMapping("/dashboard")
	public String getDashboard(HttpSession session, Model model) {
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

	@GetMapping("/update-profile")
	public String updateProfile(HttpSession session, Model model) {
		String email = (String) session.getAttribute("email");
		RegisterUsers user = registerUsersService.findUserByMail(email);
		model.addAttribute("user", user);
		model.addAttribute("mainContent", "update-profile");
		model.addAttribute("heading", "Update Profile");
		return "dashboard-layout";
	}

	@PostMapping("/update")
	public String saveUpdatedProfile(@ModelAttribute RegisterUsers user, HttpSession session) {
		String email = (String) session.getAttribute("email");
		user.setEmail(email);
		registerUsersService.registerUser(user);
		session.invalidate();
		return "redirect:/sign-in";
	}
	
	@GetMapping("/feedback")
	public String feedack(Model model) {
		model.addAttribute("mainContent", "feedback-form");
		model.addAttribute("heading", "Feedack");
		return "dashboard-layout";
	}

	@PostMapping("/submit-feedback")
	public String submitFeedback(@ModelAttribute ApplicationFeedback feedback, HttpSession session, RedirectAttributes redirectAttributes) {
		String email = (String) session.getAttribute("email");
		feedback.setEmail(email);
		feedback.setNotified(false);
		feedbackRepositor.save(feedback);
		redirectAttributes.addFlashAttribute("toastMessage", "Feedback Submitted !!");
		return "redirect:/dashboard";
	}
	
	
	@PostMapping("/submit-contact")
    public String submitContactForm(@ModelAttribute ContactMessage contact, Model model) {
        contactRepo.save(contact);
        model.addAttribute("toastMessage", "Your message has been sent successfully!");
        return "redirect:/contact"; // Assuming a contact page route
    }
}
