package com.jobapplication.org.service;


import com.jobapplication.org.entities.AppRole;
import com.jobapplication.org.entities.Jobs;
import com.jobapplication.org.entities.User;
import com.jobapplication.org.repository.JobRepository;
import com.jobapplication.org.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService{
    @Autowired
    JobRepository jobRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }


    public List<Jobs> findAll() {
        return jobRepository.findAll();
    }


    public void createJob(Jobs job) {
        jobRepository.save(job);
    }


    public Jobs getbyId(Long id) {
        return jobRepository.findById(id).orElse(null);
    }


    public Boolean removeById(Long id) {
        try{
            jobRepository.deleteById(id);
            return true;
        }catch (Exception e){
            return false;
        }
    }


    public Boolean updateById(Long id, Jobs updatedJob) {
        Optional<Jobs> optional = jobRepository.findById(id);
            if(optional.isPresent()){
                Jobs job = optional.get();
                job.setTitle(updatedJob.getTitle());
                job.setDescription(updatedJob.getDescription());
                job.setMinSalary(updatedJob.getMinSalary());
                job.setMaxSalary(updatedJob.getMaxSalary());
                job.setLocation(updatedJob.getLocation());
                job.setCompany(updatedJob.getCompany());

                return true;
            }

        return false;
    }


    public void sendApplyEmail(String email,String jobPosition,String companyName,String recruiterName,String recruiterContact, String recruiterEmail){
        emailService.sendEmail(email,
                "Acknowledgment of Your Application for "+jobPosition+" Position",
                "Dear "+userRepository.findByEmail(email).get().getUserName()+",\n" +
                        "\n" +
                        "Thank you for applying for the "+jobPosition+" at "+companyName+". We have received your application and appreciate your interest in joining our team.\n" +
                        "\n" +
                        "Our recruitment team is currently reviewing your submission, and we will update you on the status of your application as soon as possible. Should you need any further information in the meantime, feel free to reach out to us.\n" +
                        "\n" +
                        "Thank you again for considering "+companyName+". We look forward to reviewing your application.\n" +
                        "\n" +
                        "Best regards,\n" +
                        recruiterName+"\n" +
                        "Recruiter\n" +
                        companyName+"\n" +
                        recruiterContact+"\n" +
                        recruiterEmail);
    }
    public void rejectionMailToAllExcept(String email, String jobPosition, String companyName,
                                         String recruiterName, String recruiterContact,
                                         String recruiterEmail) {
        List<User> users = userRepository.findAllExceptEmailWithRole(email, AppRole.ROLE_JOBSEEKER);
        for (User userRejected : users) {
            try {
                emailService.sendEmail(userRejected.getEmail(),
                        "Acknowledgment of Your Application for " + jobPosition + " Position",
                        "Dear " + userRejected.getUserName() + ",\n" +
                                "\n" +
                                "Thank you for your interest in the " + jobPosition + " position at " + companyName + ". We appreciate the time you took to apply and the effort you put into your application.\n" +
                                "\n" +
                                "After careful consideration, we regret to inform you that we will not be moving forward with your application at this time. This decision was not easy, as we had many qualified candidates.\n" +
                                "\n" +
                                "We encourage you to apply for future openings that match your skills and experience, as we would like to keep your application on file for consideration in upcoming positions.\n" +
                                "\n" +
                                "Thank you once again for your interest in " + companyName + ". We wish you the best of luck in your job search and future endeavors.\n" +
                                "\n" +
                                "Best regards,\n" +
                                "\n" +
                                recruiterName + "\n" +
                                "Recruiter\n" +
                                companyName + "\n" +
                                recruiterContact + "\n" +
                                recruiterEmail);
            } catch (Exception e) {
                // Log the error for debugging
                e.printStackTrace();
            }
        }
    }
    public void sendHiringEmail(String candidateEmail, String candidateName, String jobPosition, String companyName, String recruiterName, String recruiterContact, String recruiterEmail) {
        String subject = "Congratulations! You've Been Hired for the " + jobPosition + " Position";

        String message = "Dear " + candidateName + ",\n\n" +
                "We are pleased to inform you that you have been selected for the " + jobPosition + " position at " + companyName + ".\n\n" +
                "We would like to invite you to our office to discuss further details regarding your salary and the hiring process. Please let us know your availability for the meeting.\n\n" +
                "Thank you for your interest in joining our team. We look forward to seeing you soon!\n\n" +
                "Best regards,\n" +
                recruiterName + "\n" +
                "Recruiter\n" +
                companyName + "\n" +
                recruiterContact + "\n" +
                recruiterEmail;

        // Sending the email using your email service
        emailService.sendEmail(candidateEmail, subject, message);
    }



}
