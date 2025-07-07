
package com.fintech.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.api.domain.Bank;

public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional <Bank> findByBankName(String bankName);
}
