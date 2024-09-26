package com.jobapplication.org.controller;

import com.jobapplication.org.entities.*;
import com.jobapplication.org.repository.ApplicationRepository;
import com.jobapplication.org.repository.CompanyRepository;
import com.jobapplication.org.repository.JobRepository;
import com.jobapplication.org.repository.UserRepository;
import com.jobapplication.org.service.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/recruiter")
public class RecruiterController {

    private final EmailService emailService;
    private final ApplicationService applicationService;
    private final JobService jobService;
    private final CompanyService companyService;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    @Autowired
    public RecruiterController(EmailService emailService, ApplicationService applicationService, JobService jobService, CompanyService companyService, JobRepository jobRepository, UserRepository userRepository, ApplicationRepository applicationRepository) {
        this.emailService = emailService;
        this.applicationService = applicationService;
        this.jobService = jobService;
        this.companyService = companyService;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }

    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    @GetMapping("/dashboard")
    public String dashboard() {
        return "recruiter/AdminHome";
    }

    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    @GetMapping("/recruit")
    public String recruiterDashboard(Model model) {
        model.addAttribute("job", new Jobs());
        return "recruiter/jobinput";
    }

    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    @GetMapping("/myListings")
    public String getMyListings(Model model, Principal principal) {
        // Retrieve job listings for the recruiter
        String recruiterEmail = principal.getName();
        List<Jobs> jobListings = jobRepository.findByCompanyRecruiterEmail(recruiterEmail);
        model.addAttribute("jobListings", jobListings);
        return "recruiter/listing"; // View name for the Thymeleaf template
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
            return userRepository.findByEmail(userDetailsImpl.getUsername())
                    .orElseThrow(() -> new RuntimeException("Recruiter not found"));
        }
        throw new RuntimeException("No authentication found");
    }

    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    @GetMapping("/listing/edit/{id}")
    public String editJobListing(@PathVariable Long id, Model model) {
        Jobs listing = jobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job Listing not found"));
        model.addAttribute("listing", listing);
        return "recruiter/editListing"; // Edit page
    }

    // Delete Job Listing and Select Candidate for Offer
    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    @GetMapping("/listing/delete/{id}")
    public String deleteJobListing(@PathVariable Long id, Model model) {
        Jobs listing = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid listing ID:" + id));

        // Get the list of applicants for this job listing
        List<Application> applicants = applicationRepository.findByJobId(id);

        model.addAttribute("applicants", applicants);
        model.addAttribute("listingId", id); // Pass the listing ID to the view
        return "recruiter/select-candidate"; // A view where the recruiter selects the chosen candidate
    }

    // Confirm the job deletion and send emails to candidates
    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    @PostMapping("/listing/delete/{id}/confirm")
    @Transactional
    public String confirmDeletion(@PathVariable Long id, @RequestParam String selectedEmail, Model model) {
        try {
            Jobs listing = jobRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid listing ID: " + id));

            // Ensure the email sending logic works as expected
            jobService.rejectionMailToAllExcept(selectedEmail, listing.getTitle(), listing.getCompany().getName(),
                    listing.getCompany().getRecruiterName(), listing.getCompany().getRecruiterContact(),
                    listing.getCompany().getRecruiterEmail());
            jobService.sendHiringEmail(selectedEmail,userRepository.findByEmail(selectedEmail).get().getUserName(), listing.getTitle(), listing.getCompany().getName(),
                    listing.getCompany().getRecruiterName(), listing.getCompany().getRecruiterContact(),
                    listing.getCompany().getRecruiterEmail());
            // First, delete applications related to this job
            applicationRepository.deleteByJobId(id);
            // Then delete the job
            jobRepository.deleteById(id);
            model.addAttribute("message", "Job listing successfully deleted.");

            return "redirect:/recruiter/myListings"; // Ensure correct redirect URL

        } catch (Exception e) {
            // Log the error for debugging purposes
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while deleting the job listing: " + e.getMessage());
            return "recruiter/listing"; // Redirect to the listings page with an error message
        }
    }


    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    @PostMapping("/job-process")
    public String createJobs(@ModelAttribute("job") Jobs job, Model model) {
        try {
            if (job.getCompany() == null) {
                model.addAttribute("error", "Company details are required.");
                return "recruiter/jobinput";
            }
            Company company = job.getCompany();
            companyService.createCompany(company);
            job.setCompany(company);
            jobService.createJob(job);
            notifyJobSeekers(job);

            return "redirect:/recruiter/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while processing the job.");
            return "recruiter/jobinput";
        }
    }
    private void notifyJobSeekers(Jobs job) {
        List<User> jobSeekers = userRepository.findByRole(AppRole.ROLE_JOBSEEKER);
        for (User jobSeeker : jobSeekers) {
            emailService.sendEmail(jobSeeker.getEmail(),
                    "New Job Posted: " + job.getTitle(),
                    createJobNotificationEmailBody(job, jobSeeker.getUserName()));
        }
    }

    private String createJobNotificationEmailBody(Jobs job, String userName) {
        return "<h3>Hi " + userName + ",</h3>" +
                "<p>A new job has been posted that matches your interests:</p>" +
                "<div style='border: 1px solid #ccc; padding: 10px; margin: 10px 0;'>" +
                "<h4>Job Title: " + job.getTitle() + "</h4>" +
                "<p>Company: " + job.getCompany().getName() + "</p>" +
                "<p>Location: " + job.getLocation() + "</p>" +
                "<p>Description: " + job.getDescription() + "</p>" +
                "<a href='job-details-url/" + job.getId() + "'>View Job Details</a>" +
                "</div>" +
                "<p>Best regards,<br>Your Job Application Team</p>";
    }

    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeJobById(@PathVariable Long id) {
        Boolean deleted = jobService.removeById(id);
        if (deleted)
            return ResponseEntity.ok("Successfully removed the job");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found");
    }

    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateJob(@PathVariable Long id, @RequestBody Jobs updatedJob) {
        Boolean updated = jobService.updateById(id, updatedJob);
        if (updated)
            return ResponseEntity.ok("Job updated successfully");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found");
    }

    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    @GetMapping("/view-applications")
    public String viewApplications(Model model, Principal principal) {
        String recruiterEmail = principal.getName();  // Get the current recruiter’s email
        List<Application> applications = applicationService.getApplicationsByRecruiter(recruiterEmail);  // Fetch applications for jobs posted by the recruiter
        model.addAttribute("applications", applications);
        return "recruiter/view-application";
    }

    @PreAuthorize("hasRole('ROLE_RECRUITER')")
    @GetMapping("/view-application/{applicationId}/resume")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long applicationId) {
        Application application = applicationService.getApplicationById(applicationId);
        if (application != null && application.getResumePath() != null) {
            try {
                Path resumePath = Paths.get(application.getResumePath());
                Resource resource = new UrlResource(resumePath.toUri());

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resumePath.getFileName() + "\"")
                        .body(resource);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
