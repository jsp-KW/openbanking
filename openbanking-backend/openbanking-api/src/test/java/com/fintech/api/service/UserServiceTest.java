package com.fintech.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fintech.api.domain.User;
import com.fintech.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)// junit에 mockito 확장기능
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;// 가짜 jpa repository

    @Mock
    private PasswordEncoder passwordEncoder;// 원하는 문자열 반환하게 세팅할 수 있는 가짜 인코더

    @InjectMocks // UserService 객체를 만들면서 위에서 만든 Mock들을 주입 
    private UserService userService; 

    
    
    @Test
    void createUser_비밀번호암호화성공() {

        // 비밀번호 1234 아직 인코딩 x
        User rawUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("1234")
                .build();

        // when 안의 함수 호출되면 then Return 반환 행동 세팅
        // when 안의 userRepository.save 가 호출 되면, db에서 새로운 user 반환하는 것이 아닌 넘어온 User 객체를 그대로 반환
        // 첫번째 인자를 그대로
        when(passwordEncoder.encode("1234")).thenReturn("encodedPw");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.createUser(rawUser);

        assertEquals("encodedPw", saved.getPassword()); 
        verify(passwordEncoder).encode("1234"); //한번 호출됐는지
        verify(userRepository).save(any(User.class)); // 저장 로직도 실제로 수행되었는지
    }

    @Test
    void isEmailExists_true반환() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);

        assertTrue(userService.isEmailExists("a@b.com"));
    }

    @Test
    void isEmailExists_false반환() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);

        assertFalse(userService.isEmailExists("a@b.com"));
    }

    @Test
    void 이메일로_userId조회_성공() {
        User user = User.builder().id(5L).email("test@example.com").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Long userId = userService.getUserIdByEmail("test@example.com");

        assertEquals(5L, userId);// 기대값과 일치하는지
    }

    @Test
    void 이메일로_userId조회_실패() {
        when(userRepository.findByEmail("none@example.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.getUserIdByEmail("none@example.com");
        });

        assertEquals("해당 이메일의 사용자가 없습니다.", ex.getMessage());
    }

    @Test
    void getUserEntityById_성공() {
        User user = User.builder().id(10L).email("x@y.com").build();
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        User result = userService.getUserEntityById(10L);

        assertEquals(10L, result.getId());
    }

    @Test
    void getUserEntityById_실패() {
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserEntityById(100L);
        });

        assertEquals("사용자를 찾을 수 없습니다.", ex.getMessage());
    }

    @Test
    void deleteUser_호출확인() {
        userService.deleteUser(7L);

        verify(userRepository).deleteById(7L);
    }
}
