package com.fintech.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import com.fintech.api.domain.User;
import com.fintech.api.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import com.fintech.api.dto.UserWithAccountsDto;
import com.fintech.api.dto.UserDto;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    // 모든 사용자를 조회 -> UserDto 리스트로 response
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllusers();
        List<UserDto> dtos = users.stream().map(UserDto::from).toList();
        return ResponseEntity.ok(dtos);
    }
    
    // 단일 사용자 id로 조회 -> UserWithAccountsDto 로 response
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<UserWithAccountsDto> getUserById(@PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User  user = userService.getUserById(id).orElseThrow(()->new IllegalArgumentException("사용자 존재x"));

        if (!user.getEmail().equals(userDetails.getUsername())){
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(UserWithAccountsDto.from(user));
    }

    // 사용자 생성-> UserDto 로 response
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.ok(UserDto.from(created));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser (@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails)  {
       User user = userService.getUserById(id).orElseThrow(()-> new IllegalArgumentException("사용자 존재하지 않습니다."));

       boolean isOwner = user.getEmail().equals(userDetails.getUsername());
       boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(auth-> auth.getAuthority().equals("ROLE_ADMIN"));


       if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).build();
       }

       
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

}
