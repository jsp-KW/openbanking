package com.fintech.api.service;

import com.fintech.api.domain.User;
import com.fintech.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Spring Security가 로그인할 때 이 메서드 호출함
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        System.out.println(">> DB 비밀번호: " + user.getPassword());
        System.out.println(">> 사용자 권한: " + user.getRole());
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail()) // email을 username처럼 사용
            .password(user.getPassword())
            .authorities("ROLE_" + user.getRole().name())
            .build();
    }
}
