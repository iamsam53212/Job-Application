package com.jobapplication.org.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private Double rating;

    @JsonIgnore
    @ManyToOne
    private Company company;

    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)  // Lazy loading to prevent unnecessary fetching
    @JoinColumn(name = "user_id", nullable = false) // Foreign key to the User entity
    private User user;

    public Review() {
    }

    public Review(Long id, String title, String description, Double rating, Company company, User user) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.rating = rating;
        this.company = company;
        this.user = user;
    }

    public Long getUserId() {
        return user != null ? user.getUserId() : null;
    }

}
