
package com.fintech.api.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.api.domain.Transaction;

public interface TransactionRepository  extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

}
