package com.example.Job_Application_Tracker.service;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
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

    @Scheduled(fixedRate = 90000)
    public void sendNoUpdateReminders() throws Exception {
        LocalDate cutoff = LocalDate.now().minusDays(14);

        List<JobApplication> allJobs = jobRepo.findAll().stream()
            .filter(j -> j.getEmail() != null)
            .collect(Collectors.toList());

        Map<String, List<JobApplication>> grouped = allJobs.stream()
            .filter(j -> "Applied".equalsIgnoreCase(j.getStatus()))
            .filter(j -> j.getDateApplied() != null && j.getDateApplied().isBefore(cutoff))
            .collect(Collectors.groupingBy(JobApplication::getEmail));

        for (Map.Entry<String, List<JobApplication>> entry : grouped.entrySet()) {
            String email = entry.getKey();
            List<JobApplication> jobs = entry.getValue();

            if (jobs.isEmpty()) continue;

            StringBuilder rows = new StringBuilder();
            for (JobApplication job : jobs) {
                rows.append("<tr><td>").append(job.getCompanyName())
                    .append("</td><td>").append(job.getRoleName())
                    .append("</td><td>").append(job.getDateApplied())
                    .append("</td></tr>");
            }

            sendEmail(email, "⏳ Job Applications Reminder", "no-update-mail.html",
                    Map.of("{{tableBody}}", rows.toString()));
        }
    }

    @Scheduled(fixedRate = 90000)
    public void sendWeeklySummary() throws Exception {
        LocalDate from = LocalDate.now().minusDays(7);
        List<JobApplication> allJobs = jobRepo.findAll().stream()
            .filter(j -> j.getEmail() != null && j.getDateApplied() != null)
            .collect(Collectors.toList());

        Set<String> users = allJobs.stream()
            .map(JobApplication::getEmail)
            .collect(Collectors.toSet());

        for (String email : users) {
            long applied = allJobs.stream()
                .filter(j -> email.equals(j.getEmail()) && !j.getDateApplied().isBefore(from))
                .count();

            long shortlisted = allJobs.stream()
                .filter(j -> email.equals(j.getEmail())
                        && "SHORTLISTED".equalsIgnoreCase(j.getStatus())
                        && !j.getDateApplied().isBefore(from))
                .count();

            long rejected = allJobs.stream()
                .filter(j -> email.equals(j.getEmail())
                        && "REJECTED".equalsIgnoreCase(j.getStatus())
                        && !j.getDateApplied().isBefore(from))
                .count();

            if (applied == 0 && shortlisted == 0 && rejected == 0) continue;

            sendEmail(email, "📊 Weekly Job Summary", "weekly-summary-mail.html",
                Map.of("{{applied}}", String.valueOf(applied),
                       "{{shortlisted}}", String.valueOf(shortlisted),
                       "{{rejected}}", String.valueOf(rejected)));
        }
    }

    @Scheduled(fixedRate = 90000)
    public void sendMonthlySummary() throws Exception {
        LocalDate from = LocalDate.now().minusMonths(1);
        List<JobApplication> allJobs = jobRepo.findAll().stream()
            .filter(j -> j.getEmail() != null && j.getDateApplied() != null)
            .collect(Collectors.toList());

        Set<String> users = allJobs.stream()
            .map(JobApplication::getEmail)
            .collect(Collectors.toSet());

        for (String email : users) {
            long applied = allJobs.stream()
                .filter(j -> email.equals(j.getEmail()) && !j.getDateApplied().isBefore(from))
                .count();

            long shortlisted = allJobs.stream()
                .filter(j -> email.equals(j.getEmail())
                        && "SHORTLISTED".equalsIgnoreCase(j.getStatus())
                        && !j.getDateApplied().isBefore(from))
                .count();

            long rejected = allJobs.stream()
                .filter(j -> email.equals(j.getEmail())
                        && "REJECTED".equalsIgnoreCase(j.getStatus())
                        && !j.getDateApplied().isBefore(from))
                .count();

            if (applied == 0 && shortlisted == 0 && rejected == 0) continue;

            sendEmail(email, "📈 Monthly Job Summary", "monthly-summary-mail.html",
                Map.of("{{applied}}", String.valueOf(applied),
                       "{{shortlisted}}", String.valueOf(shortlisted),
                       "{{rejected}}", String.valueOf(rejected)));
        }
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
