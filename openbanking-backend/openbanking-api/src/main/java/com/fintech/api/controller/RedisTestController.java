package com.fintech.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fintech.api.service.RedisTestService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/redis")

public class RedisTestController {
    private final RedisTestService redisTestService;
    
    @PostMapping("/save") 
    public ResponseEntity <Void> save() {
        redisTestService.saveTest();
        return ResponseEntity.ok().build();
    }

    @GetMapping ("/get")
    public ResponseEntity<String> get() {
        return ResponseEntity.ok(redisTestService.getTest());
    }
}
