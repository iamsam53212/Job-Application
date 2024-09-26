package com.jobapplication.org.service;

import com.jobapplication.org.entities.Company;
import com.jobapplication.org.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService{

    private CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }


    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }


    public Boolean updateCompany(Company company, Long id) {
        Optional<Company> optional = companyRepository.findById(id);
        if(optional.isPresent()){
            Company companyToUpdate = optional.get();
            companyToUpdate.setName(company.getName());
            companyToUpdate.setDescription(company.getDescription());
            companyToUpdate.setJobs(company.getJobs());
            companyRepository.save(companyToUpdate);
            return true;
        }
        else
            return false;
    }

    public Company getCompanyByEmail(String email){
        return companyRepository.findByRecruiterEmail(email);
    }

    public void createCompany(Company company) {
        Company existingCompany = companyRepository.findByName(company.getName());
        if (existingCompany == null) {
            companyRepository.save(company);
        }
        else {
            company.setId(existingCompany.getId());
        }
    }

    public Boolean removeById(Long id) {
        if(companyRepository.existsById(id)){
            companyRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id).orElse(null);
    }
}
