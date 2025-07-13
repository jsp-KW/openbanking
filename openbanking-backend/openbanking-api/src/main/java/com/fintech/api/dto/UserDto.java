package com.fintech.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.fintech.api.domain.User;
import com.fintech.api.domain.Role;

// 사용자 정보만 응답하도록
// 비밀번호와 같은 민감한 private information 은 remove

@Getter
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    // password는 보안문제로  포함 xxx

    public static UserDto from (User user) {
        return new UserDto(
            user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole()
        );
    }
}
