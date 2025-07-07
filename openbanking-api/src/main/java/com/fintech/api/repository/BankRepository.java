
package com.fintech.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.api.domain.Bank;

// JpaRepostiory <Bank,Long> 를 상속하므로
// findById(Long id), findAll(), save(Bank bank), deleteById(Long id) 기본적으로 포함
public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional <Bank> findByBankName(String bankName);
}
