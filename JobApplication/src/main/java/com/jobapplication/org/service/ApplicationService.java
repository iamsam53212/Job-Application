package com.jobapplication.org.service;

import com.jobapplication.org.entities.Application;
import com.jobapplication.org.entities.Jobs;
import com.jobapplication.org.repository.ApplicationRepository;
import com.jobapplication.org.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Application getApplicationById(Long id) {
        Optional<Application> optionalApplication = applicationRepository.findById(id);
        return optionalApplication.orElse(null); // Or throw an exception if not found
    }
    public List<Application> getApplicationsByRecruiter(String recruiterEmail) {
        // Find all jobs posted by this recruiter
        List<Jobs> jobsPostedByRecruiter = jobRepository.findByCompanyRecruiterEmail(recruiterEmail);

        // Fetch applications related to the jobs posted by the recruiter
        return applicationRepository.findByJobIn(jobsPostedByRecruiter);
    }
}