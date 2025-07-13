package com.fintech.api.dto;
import com.fintech.api.domain.Role;
public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private Role role; // "USER" or "ADMIN"
   
}
