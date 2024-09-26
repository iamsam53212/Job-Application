package com.jobapplication.org.controller;

import com.jobapplication.org.entities.AppRole;
import com.jobapplication.org.entities.Role;
import com.jobapplication.org.entities.User;
import com.jobapplication.org.jwt.JwtUtils;
import com.jobapplication.org.repository.RoleRepository;
import com.jobapplication.org.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping
public class HomeController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    //Redirecting login page
    @GetMapping("/login")
    public String login(){
        return "login";
    }

    //redirecting signup page
    @GetMapping("/signup")
    public String signupPage(){
        return "signup";
    }


    @PostMapping("/signup-process")
    public String signup(@RequestParam String userName,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String role,
                         Model model) {

        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        // Retrieve existing role from the database
        System.out.println("Attempting to find role for: ROLE_" + role.toUpperCase());
        Role userRole = roleRepository.findByRoleName(AppRole.valueOf("ROLE_" + role.toUpperCase()));

        if (userRole == null) {
            throw new IllegalArgumentException("Role not found: " + role);
        }
        user.setRole(userRole);

        // Automatically set default values
        user.setAccountExpiryDate(LocalDate.now().plusYears(3));
        user.setCredentialsExpiryDate(LocalDate.now().plusYears(3));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        user.setTwoFactorSecret(null);
        user.setCreatedDate(LocalDateTime.now());
        user.setUpdatedDate(LocalDateTime.now());

        userRepository.save(user);
        System.out.println("User added Successfully");
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home(){
        return "home";
    }


    @GetMapping("/service")
    public String service(){
        return "service";
    }

    @GetMapping("/logout")
    public String logout() {
        // Redirect to the home page after logout
        return "redirect:/";
    }

}
