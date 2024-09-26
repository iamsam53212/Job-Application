package com.jobapplication.org.entities.resp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginResponse {
    private String jwtToken;

    private String email;
    private List<String> roles;

    public LoginResponse(String email, List<String> roles, String jwtToken) {
        this.email = email;
        this.roles = roles;
        this.jwtToken = jwtToken;
    }
}