package com.jobapplication.org.controller;

import com.jobapplication.org.entities.Company;
import com.jobapplication.org.entities.Review;
import com.jobapplication.org.entities.User;
import com.jobapplication.org.repository.UserRepository;
import com.jobapplication.org.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/companies")
public class CompanyController {

    private UserRepository userRepository;
    private CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService,UserRepository userRepository) {
        this.companyService = companyService;
        this.userRepository=userRepository;
    }

    @PreAuthorize("hasAuthority('OAUTH2_USER') or hasAuthority('ROLE_JOBSEEKER') or hasAuthority('OIDC_USER') or hasAuthority('SCOPE_https://www.googleapis.com/auth/userinfo.email') or hasAuthority('SCOPE_https://www.googleapis.com/auth/userinfo.profile') or hasAuthority('SCOPE_openid')")
    @GetMapping("/details/{id}")
    public String getCompanyDetails(@PathVariable Long id, Model model) {
        System.out.println(companyService.getCompanyById(id).getName());
        Company company = companyService.getCompanyById(id);
        if (company != null) {
            List<Review> reviews = company.getReviews();
            Map<Review, User> reviewUserMap = new HashMap<>();

            for (Review review : reviews) {
                User user = userRepository.findById(review.getUserId())
                        .orElseThrow(RuntimeException::new); // Assuming getUserById fetches the user by ID
                reviewUserMap.put(review, user);
            }

            model.addAttribute("company", company);
            model.addAttribute("reviewUserMap", reviewUserMap);
            return "company-details"; // Thymeleaf template name
        }
        return "redirect:/index"; // Redirect to home or error page
    }


    @GetMapping
    public ResponseEntity<List<Company>> getAllCompaniesDetails(){
        return new ResponseEntity<>(companyService.getAllCompanies(),HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCompanyDetail(@RequestBody Company company ,
                                                      @PathVariable Long id){

        companyService.updateCompany(company,id);
        return new ResponseEntity<>("Company Updated Successfully!", HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> addCompany(@RequestBody Company company){
        companyService.createCompany(company);
        return new ResponseEntity<>("Company added Successfully!",HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCompanyDetails(@PathVariable Long id){
        Boolean isDeleted = companyService.removeById(id);
        if(isDeleted)
            return new ResponseEntity<>("Company Deleted Successfully!",HttpStatus.OK);
        return new ResponseEntity<>("Company not found!",HttpStatus.NOT_FOUND);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Company> findCompany(@PathVariable Long id){
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }
}
