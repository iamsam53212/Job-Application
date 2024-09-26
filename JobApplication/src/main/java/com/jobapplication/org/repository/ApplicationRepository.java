package com.jobapplication.org.repository;

import com.jobapplication.org.entities.Application;
import com.jobapplication.org.entities.Company;
import com.jobapplication.org.entities.Jobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application,Long> {

    @Query("SELECT a FROM Application a JOIN FETCH a.job j JOIN FETCH j.company WHERE a.id = :id")
    Application findByIdWithJobAndCompany(@Param("id") Long id);

    List<Application> findByJobIn(List<Jobs> jobs);

    List<Application> findByJobId(Long id);

    void deleteByJobId(Long id);
}
