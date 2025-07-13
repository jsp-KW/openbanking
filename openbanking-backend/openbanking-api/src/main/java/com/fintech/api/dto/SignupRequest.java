package com.fintech.api.dto;
import com.fintech.api.domain.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private Role role; // "USER" or "ADMIN"
   
}
