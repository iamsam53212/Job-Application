package com.jobapplication.org.repository;


import com.jobapplication.org.entities.Jobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JobRepository extends JpaRepository<Jobs, Long> {
    List<Jobs> findByCompanyRecruiterEmail(String recruiterEmail);
}
