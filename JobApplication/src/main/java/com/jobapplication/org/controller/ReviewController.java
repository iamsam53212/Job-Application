package com.jobapplication.org.controller;

import com.jobapplication.org.entities.Review;
import com.jobapplication.org.entities.User;
import com.jobapplication.org.repository.CompanyRepository;
import com.jobapplication.org.repository.ReviewRepository;
import com.jobapplication.org.repository.UserRepository;
import com.jobapplication.org.service.ReviewService;
import com.jobapplication.org.service.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/companies/{companyId}")
public class ReviewController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    private ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getAllReviews(@PathVariable Long companyId){
        return ResponseEntity.ok(reviewService.getAllReviews(companyId));
    }

    @PostMapping("/addReview")
    public String addReview(@PathVariable Long companyId, Review review) {
        // Assuming you have a way to get the current user's ID or object
        User currentUser = getCurrentUser(); // Implement this method to fetch the authenticated user
        if (currentUser != null) {
            review.setUser(currentUser);
            review.setDate(LocalDateTime.now());
            review.setCompany(companyRepository.findById(companyId).orElseThrow(() -> new EntityNotFoundException("Company not found")));
            reviewRepository.save(review);
        } else {
            throw new IllegalStateException("User must be logged in to add a review.");
        }
        return "redirect:/jobseeker/home";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
            String username = userDetailsImpl.getUsername();

            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found for username: " + username));
        }
        throw new RuntimeException("No authentication found.");
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long companyId,
                                                @PathVariable Long reviewId ){
        return ResponseEntity.ok(reviewService.getReview(companyId,reviewId));
    }
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable Long companyId,
                                               @PathVariable Long reviewId,
                                               @RequestBody Review review){
        Boolean isUpdated= reviewService.updateReview(companyId,reviewId,review);
        if(isUpdated)
            return ResponseEntity.ok("Review Updated Successfully!");
        return new ResponseEntity<>("Review neither found nor updated",HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long companyId,
                                               @PathVariable Long reviewId){
        Boolean isDeleted= reviewService.deleteReview(companyId,reviewId);
        if(isDeleted)
            return ResponseEntity.ok("Review Deleted Successfully!");
        return new ResponseEntity<>("Review neither found nor deleted",
                HttpStatus.NOT_FOUND);
    }
}
