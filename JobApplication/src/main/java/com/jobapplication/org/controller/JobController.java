package com.jobapplication.org.controller;

import com.jobapplication.org.entities.Jobs;
import com.jobapplication.org.entities.User;
import com.jobapplication.org.repository.UserRepository;
import com.jobapplication.org.service.CompanyService;
import com.jobapplication.org.service.JobService;
import com.jobapplication.org.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobseeker")
public class JobController {

    @Autowired
    private UserRepository userRepository;


    private final JobService jobService;
    private CompanyService companyService;

    @Autowired
    public JobController(JobService jobService,CompanyService companyService) {
        this.jobService = jobService;
        this.companyService=companyService;
    }

    @PreAuthorize("hasAuthority('OAUTH2_USER') or hasAuthority('ROLE_JOBSEEKER') or hasAuthority('OIDC_USER') or hasAuthority('SCOPE_https://www.googleapis.com/auth/userinfo.email') or hasAuthority('SCOPE_https://www.googleapis.com/auth/userinfo.profile') or hasAuthority('SCOPE_openid')")
    @GetMapping("/home")
    public String home(){
        return "home";
    }

    @PreAuthorize("hasAuthority('OAUTH2_USER') or hasAuthority('ROLE_JOBSEEKER') or hasAuthority('OIDC_USER') or hasAuthority('SCOPE_https://www.googleapis.com/auth/userinfo.email') or hasAuthority('SCOPE_https://www.googleapis.com/auth/userinfo.profile') or hasAuthority('SCOPE_openid')")
    @GetMapping("/apply/{jobId}")
    public String apply(@PathVariable Long jobId, Model model){
        Jobs job = jobService.getbyId(jobId);
        // Assuming user details are obtained from a service or session
        User user = getCurrentUser();
        if (user == null) {
            System.out.println("No user found.");
        } else {
            System.out.println("User found: " + user.getUserName());
        }
        System.out.println("Current User: " + user);
        model.addAttribute("user", user);
        model.addAttribute("job", job);
        return "apply";
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

                String username = (String) attributes.get("email"); // Adjust based on your OAuth provider

                return userRepository.findByEmail(username)
                        .orElseThrow(RuntimeException::new);
            }
        }
        return null; // Or throw new RuntimeException("No authenticated user found");
    }




    @PreAuthorize("hasAuthority('OAUTH2_USER') or hasAuthority('ROLE_JOBSEEKER') or hasAuthority('OIDC_USER') or hasAuthority('SCOPE_https://www.googleapis.com/auth/userinfo.email') or hasAuthority('SCOPE_https://www.googleapis.com/auth/userinfo.profile') or hasAuthority('SCOPE_openid')")
    @GetMapping("/jobs")
    public String findAllJobs(Model model){
        List<Jobs> jobList = jobService.findAll();
        model.addAttribute("jobs",jobList);
        return "index";
    }

    @PreAuthorize("hasAuthority('OAUTH2_USER') or hasAuthority('ROLE_JOBSEEKER') or hasAuthority('OIDC_USER') or hasAuthority('SCOPE_https://www.googleapis.com/auth/userinfo.email') or hasAuthority('SCOPE_https://www.googleapis.com/auth/userinfo.profile') or hasAuthority('SCOPE_openid')")
    @GetMapping("/{id}")
    public String getJobById(@PathVariable Long id, Model model){
        Jobs job = jobService.getbyId(id);
        if(job!=null){
            model.addAttribute("jobs",job);
            return "index";
        }
        return "error/404";
    }

}
