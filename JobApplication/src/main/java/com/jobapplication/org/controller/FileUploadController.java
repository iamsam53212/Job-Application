package com.jobapplication.org.controller;

import com.jobapplication.org.entities.Application;
import com.jobapplication.org.entities.Company;
import com.jobapplication.org.entities.Jobs;
import com.jobapplication.org.entities.User;
import com.jobapplication.org.repository.ApplicationRepository;
import com.jobapplication.org.repository.UserRepository;
import com.jobapplication.org.service.EmailService;
import com.jobapplication.org.service.JobService;
import com.jobapplication.org.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class FileUploadController {

    @Autowired
    private JobService jobService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private static final String UPLOAD_DIR = "C:/Users/Isha Attique/Desktop/uploads"; // Updated path

    @PostMapping("/apply")
    public String apply(@RequestParam("fullName") String fullName,
                        @RequestParam("email") String email,
                        @RequestParam("position") String position,
                        @RequestParam("coverLetter") String coverLetter,
                        @RequestParam("resume") MultipartFile file,
                        @RequestParam("jobId") Long jobId,
                        Model model) {

        if (jobId == null) {
            model.addAttribute("message", "Job ID is missing.");
            return "apply";
        }

        Jobs job = jobService.getbyId(jobId);
        if (job == null) {
            model.addAttribute("message", "Job not found.");
            return "apply";
        }

        // Handle resume file upload
        String resumePath = null;
        if (!file.isEmpty()) {
            try {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                File destinationFile = new File(uploadDir, file.getOriginalFilename());
                file.transferTo(destinationFile);
                resumePath = Paths.get(UPLOAD_DIR, file.getOriginalFilename()).toString();
            } catch (IOException e) {
                model.addAttribute("message", "Failed to upload file: " + e.getMessage());
                return "apply";
            }
        }
        Company company = job.getCompany();

        // Create and save application details
        Application application = new Application();
        application.setFullName(fullName);
        application.setEmail(email);
        application.setPositionAppliedFor(position);
        application.setCoverLetter(coverLetter);
        application.setResumePath(resumePath);
        application.setJob(job);
        application.setApplicationDate(LocalDateTime.now());
        application.setUser(getCurrentUser());

        applicationRepository.save(application);

        jobService.sendApplyEmail(email,position, company.getName(), company.getRecruiterName(), company.getRecruiterContact(),company.getRecruiterEmail());

        model.addAttribute("message", "Application submitted successfully.");
        return "home";
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // Check if the user is authenticated via traditional username/password
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
                String username = userDetailsImpl.getUsername(); // Assuming the username is the email
                return userRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("User not found")); // Handle absence here
            }

            // Check if the user is authenticated via OAuth2
            else if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                Map<String, Object> attributes = oAuth2User.getAttributes();
                System.out.println("OAuth2 User Attributes: " + attributes);

                String email = (String) attributes.get("email"); // Adjust based on your OAuth provider

                return userRepository.findByEmail(email)
                        .orElseThrow(RuntimeException::new);
            }
        }

        // Return null or throw an exception if no authenticated user is found
        return null; // Or throw new RuntimeException("No authenticated user found");
    }
}
