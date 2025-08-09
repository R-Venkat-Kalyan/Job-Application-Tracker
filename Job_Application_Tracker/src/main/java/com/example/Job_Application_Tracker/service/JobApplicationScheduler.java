package com.example.Job_Application_Tracker.service;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.Job_Application_Tracker.entity.JobApplication;
import com.example.Job_Application_Tracker.repository.JobApplicationRepository;

@Component
public class JobApplicationScheduler {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private JobApplicationRepository jobRepo;

	private static final String FROM_EMAIL = "taskprompter@gmail.com";

//	@Scheduled(fixedRate = 60000) // for testing every 1 minutes
	@Scheduled(cron = "0 0 10 */14 * *") // real: every 2 weeks at 10 AM
	public void sendNoUpdateReminders() {
		LocalDate cutoff = LocalDate.now().minusDays(14);

		// Group jobs by user email
		Map<String, List<JobApplication>> grouped = jobRepo.findAll().stream()
				.filter(j -> j.getEmail() != null && j.getDateApplied() != null && j.getStatus() != null)
				.filter(j -> j.getStatus().equalsIgnoreCase("Applied") && j.getDateApplied().isBefore(cutoff))
				.collect(Collectors.groupingBy(JobApplication::getEmail));

		grouped.forEach((email, jobs) -> {
			if (jobs.isEmpty())
				return;

			try {
				// Build the job list as HTML bullet points
				StringBuilder jobListBuilder = new StringBuilder("<ul style='padding-left:20px;'>");
				for (JobApplication job : jobs) {
					jobListBuilder.append("<li>").append("<strong>").append(job.getCompanyName()).append("</strong>")
							.append(" — ").append(job.getRoleName()).append(" (Applied on ")
							.append(job.getDateApplied()).append(")").append("</li>");
				}
				jobListBuilder.append("</ul>");

				// Add the disclaimer in a cool style
				String disclaimer = "<div style='margin-top:20px; font-size:14px; color:#555; "
						+ "background:#fff4e5; padding:10px; border-left:4px solid #f59e0b;'>"
						+ "⚠ If any information above is incorrect, please log in to <strong>JobTrackrly</strong> "
						+ "and update your application details. Keeping your data accurate helps us help you better! 💡"
						+ "</div>";

				sendEmail(email, "⏳ Job Applications Reminder", "no-update-mail.html",
						Map.of("{{jobList}}", jobListBuilder.toString() + disclaimer));

				System.out.println("No-update reminder sent to: " + email);

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	// 📊 Weekly Summary: every Monday at 11 AM

//	@Scheduled(fixedRate = 60000) // for testing every 1 minutes
	@Scheduled(cron = "0 0 11 * * MON")
	public void sendWeeklySummary() {
		LocalDate from = LocalDate.now().minusDays(7);
		List<JobApplication> allJobs = jobRepo.findAll();

		for (JobApplication job : allJobs) {
			if (job.getEmail() == null || job.getDateApplied() == null)
				continue;

			String email = job.getEmail();

			long applied = countJobs(allJobs, email, from, null);
			long shortlisted = countJobs(allJobs, email, from, "SHORTLISTED");
			long rejected = countJobs(allJobs, email, from, "REJECTED");

			if (applied == 0 && shortlisted == 0 && rejected == 0)
				continue;

			try {
				Map<String, String> placeholders = new HashMap<>();
				placeholders.put("{{applied}}", String.valueOf(applied));
				placeholders.put("{{shortlisted}}", String.valueOf(shortlisted));
				placeholders.put("{{rejected}}", String.valueOf(rejected));

				sendEmail(email, "📊 Weekly Job Summary", "weekly-summary-mail.html", placeholders);
				System.out.println("Weekly summary sent to: " + email);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

//	@Scheduled(fixedRate = 60000) // for testing every 1 minutes

	@Scheduled(cron = "0 0 12 1 * *") // 📅 Monthly Summary: 1st day of each month at 12 PM
	public void sendMonthlySummary() {
		LocalDate from = LocalDate.now().minusMonths(1);
		List<JobApplication> allJobs = jobRepo.findAll();

		for (JobApplication job : allJobs) {
			if (job.getEmail() == null || job.getDateApplied() == null)
				continue;

			String email = job.getEmail();

			long applied = countJobs(allJobs, email, from, null);
			long shortlisted = countJobs(allJobs, email, from, "SHORTLISTED");
			long rejected = countJobs(allJobs, email, from, "REJECTED");

			if (applied == 0 && shortlisted == 0 && rejected == 0)
				continue;

			try {
				Map<String, String> placeholders = new HashMap<>();
				placeholders.put("{{applied}}", String.valueOf(applied));
				placeholders.put("{{shortlisted}}", String.valueOf(shortlisted));
				placeholders.put("{{rejected}}", String.valueOf(rejected));

				sendEmail(email, "📈 Monthly Job Summary", "monthly-summary-mail.html", placeholders);
				System.out.println("Monthly summary sent to: " + email);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private long countJobs(List<JobApplication> allJobs, String email, LocalDate from, String status) {
		return allJobs.stream().filter(j -> email.equals(j.getEmail()))
				.filter(j -> j.getDateApplied() != null && !j.getDateApplied().isBefore(from))
				.filter(j -> status == null || (j.getStatus() != null && j.getStatus().equalsIgnoreCase(status)))
				.count();
	}

	private void sendEmail(String to, String subject, String templateName, Map<String, String> placeholders)
			throws Exception {

		MimeMessagePreparator preparator = mimeMessage -> {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setFrom(FROM_EMAIL);
			helper.setTo(to);
			helper.setSubject(subject);

			Resource resource = new ClassPathResource("templates/" + templateName);
			String template = new String(Files.readAllBytes(resource.getFile().toPath()));

			for (Map.Entry<String, String> entry : placeholders.entrySet()) {
				template = template.replace(entry.getKey(), entry.getValue());
			}

			helper.setText(template, true);
		};

		mailSender.send(preparator);
	}
}
