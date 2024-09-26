package com.jobapplication.org.repository;


import com.jobapplication.org.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company,Long> {
    Company findByName(String name);

    Company findByRecruiterEmail(String email);
}
