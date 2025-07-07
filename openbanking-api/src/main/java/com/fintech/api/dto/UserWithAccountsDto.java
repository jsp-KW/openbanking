package com.fintech.api.dto;
// 계좌 목록까지 포함하는 확장형 DTO
// 사용자의 기본적인 정보 + 계좌 목록을 함께 보여주고 싶은 경우
// 복합적인 response 에 사용하는 DTO 확장 버전설계

// GET /users/1 이라는 api 호출 시
// UserDto 만 있으면 사용자의 계좌는 보여줄 수 없으므로

import java.util.List;
import java.util.stream.Collectors;

import com.fintech.api.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserWithAccountsDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private List<AccountDto> accounts;


    public static UserWithAccountsDto from (User user) {
        return new  UserWithAccountsDto(user.getId(), user.getName(), 
        user.getEmail(), user.getPhone(), user.getAccounts().stream().map(AccountDto::from).collect(Collectors.toList()));
    }
}
