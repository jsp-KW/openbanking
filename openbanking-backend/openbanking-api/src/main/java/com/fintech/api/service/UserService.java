package com.fintech.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fintech.api.domain.User;
import com.fintech.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;// 비밀번호 암호화 부분 꼭 추가해야!!


    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));// 추가
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
