package com.fintech.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fintech.api.domain.User;
import com.fintech.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;


    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserById (Long id) {
        return userRepository.findById(id);
    }

    public List <User> getAllusers() {
        return userRepository.findAll();
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
}
