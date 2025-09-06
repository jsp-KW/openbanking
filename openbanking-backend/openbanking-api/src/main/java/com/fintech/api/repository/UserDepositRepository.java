package com.fintech.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.api.domain.UserDeposit;

public interface UserDepositRepository extends JpaRepository<UserDeposit, Long> {
    List<UserDeposit> findByUserId(Long userId);
} 