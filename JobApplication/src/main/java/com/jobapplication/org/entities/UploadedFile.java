package com.jobapplication.org.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "uploaded_files")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;  // Optional: if you want to associate the file with a user

    private String fileName;
    private String filePath;
    private String fileType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadDate = new Date();

    private String status = "active";

    // Getters and setters
}
