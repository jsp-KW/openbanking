package com.fintech.api.controller;

import com.fintech.api.domain.AccountType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/account-types")
public class AccountTypeController {
    
    @GetMapping
    public List<String> getAccountTypes() {
        return Arrays.stream(AccountType.values())
        .map(AccountType::getLabel).collect(Collectors.toList());
    }
}
