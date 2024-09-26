package com.jobapplication.org.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "job_applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Jobs job;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "position_applied_for", nullable = false)
    private String positionAppliedFor;

    @Column(name = "cover_letter", nullable = false)
    private String coverLetter;

    @Column(name = "resume_path", nullable = false)
    private String resumePath;

    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;


    public Application() {
    }

    public Application(Long id,
                       String fullName,
                       String email,
                       String positionAppliedFor,
                       String coverLetter,
                       LocalDateTime applicationDate,
                       String resumePath) {
        this.id = id;
        this.user = user;
        this.fullName = fullName;
        this.email = email;
        this.positionAppliedFor = positionAppliedFor;
        this.coverLetter = coverLetter;
        this.resumePath = resumePath;
        this.applicationDate = applicationDate;
    }
}

