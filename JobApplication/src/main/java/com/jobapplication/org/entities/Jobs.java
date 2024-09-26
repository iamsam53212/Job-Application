package com.jobapplication.org.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "jobs")
public class Jobs {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String title;

    @Column
    @Size(max = 5000000)
    private String description;

    @Column
    private String minSalary;

    @Column
    private String maxSalary;

    @Column
    private String location;

    @ManyToOne
    private Company company;

    public Jobs() {
    }

    public Jobs(Long id, String title, String description, String minSalary, String maxSalary, String location) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.location = location;
    }
}
