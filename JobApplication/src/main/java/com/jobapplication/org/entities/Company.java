package com.jobapplication.org.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Entity
@Table
@Data
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String name;
    @Column
    @Size(max = 50000000)
    private String description;

    private String recruiterName;
    private String recruiterContact;
    private String recruiterEmail;

    @JsonIgnore
    @OneToMany(mappedBy = "company")
    private List<Jobs> jobs;

    @OneToMany(mappedBy = "company")
    private List<Review> reviews;
    public Company() {
    }


    public Company(Long id, String name, String description, List<Jobs> jobs) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.jobs = jobs;
    }

}
