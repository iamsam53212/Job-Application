package com.jobapplication.org.config;

import com.jobapplication.org.entities.AppRole;
import com.jobapplication.org.entities.Role;
import com.jobapplication.org.entities.User;
import com.jobapplication.org.jwt.JwtUtils;
import com.jobapplication.org.repository.RoleRepository;
import com.jobapplication.org.repository.UserRepository;
import com.jobapplication.org.service.UserDetailsImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        String jwtToken;
        List<String> roles;

        // Check if the user is logging in via OAuth2 or form login
        if (authentication.getPrincipal() instanceof OAuth2User userDetails) {
            String email = userDetails.getAttribute("email");

            // In case email is null, handle it here (for example, log the issue or throw an exception)
            if (email == null) {
                throw new IllegalStateException("Email not available from OAuth2 provider");
            }

            // Assign the role for OAuth2 users (GitHub/Gmail users as job seekers)
            String role = "ROLE_JOBSEEKER";

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setUserName(userDetails.getAttribute("name"));
                        // Assuming roles only contains one element "ROLE_JOBSEEKER"
                        Role userRole = roleRepository.findByRoleName(AppRole.valueOf(role.toUpperCase())); // Get the first role
                        newUser.setRole(userRole);
                        System.out.println(userRole);

                        newUser.setAccountExpiryDate(LocalDate.now().plusYears(3));
                        newUser.setCredentialsExpiryDate(LocalDate.now().plusYears(3));
                        newUser.setAccountNonExpired(true);
                        newUser.setAccountNonLocked(true);
                        newUser.setCredentialsNonExpired(true);
                        newUser.setEnabled(true);
                        newUser.setTwoFactorSecret(null);
                        newUser.setCreatedDate(LocalDateTime.now());
                        newUser.setUpdatedDate(LocalDateTime.now());
                        userRepository.save(newUser);
                        return newUser;
                    });
            jwtToken = jwtUtils.generateTokenFromOAuth2User(userDetails);
            roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            System.out.println(roles);
        }
        else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

            // Extract roles from UserDetailsImpl
            roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            System.out.println(roles);
        } else {
            throw new IllegalStateException("Unknown authentication principal type");
        }

        // Determine the redirect URL based on roles
        String redirectUrl = "/home"; // Default redirect URL
        if (roles.contains("ROLE_JOBSEEKER")) {
            redirectUrl = "/jobseeker/home"; // Redirect for job seekers
        } else if (roles.contains("ROLE_RECRUITER")) {
            redirectUrl = "/recruiter/dashboard"; // Redirect for recruiters
        }else if (roles.contains("OAUTH2_USER")) {
            redirectUrl = "/jobseeker/home"; // Redirect for job seekers
        }
        else if (roles.contains("OIDC_USER")) {
            redirectUrl = "/jobseeker/home"; // Redirect for job seekers
        }


        // Create redirect URL with JWT token
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("token", jwtToken)
                .build()
                .toUriString();

        // Redirect to the target URL
        response.sendRedirect(targetUrl);
    }
}
