package com.fintech.api.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.api.domain.DepositProduct;
import com.fintech.api.domain.DepositStatus;
import com.fintech.api.domain.User;
import com.fintech.api.domain.UserDeposit;

public interface UserDepositRepository extends JpaRepository<UserDeposit, Long> {
    List<UserDeposit> findByUserId(Long userId);

    List<UserDeposit> findByStatusAndMaturityDateBefore(DepositStatus inProgress, LocalDateTime now);

    boolean existsByUserAndProductAndStatus(User user, DepositProduct product, DepositStatus active);
} 