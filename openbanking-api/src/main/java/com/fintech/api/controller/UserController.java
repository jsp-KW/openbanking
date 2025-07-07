package com.fintech.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.fintech.api.domain.User;
import com.fintech.api.service.UserService;

import lombok.RequiredArgsConstructor;
import com.fintech.api.dto.UserWithAccountsDto;
import com.fintech.api.dto.UserDto;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    // 모든 사용자를 조회 -> UserDto 리스트로 response
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllusers();
        List<UserDto> dtos = users.stream().map(UserDto::from).toList();
        return ResponseEntity.ok(dtos);
    }
    // 단일 사용자 id로 조회 -> UserWithAccountsDto 로 response
    @GetMapping("/{id}")
    public ResponseEntity<UserWithAccountsDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id).map(UserWithAccountsDto::from).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
    }
    // 사용자 생성-> UserDto 로 response
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.ok(UserDto.from(created));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser (@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

}
