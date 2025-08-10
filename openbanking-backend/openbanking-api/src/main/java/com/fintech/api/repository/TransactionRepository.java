
package com.fintech.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.api.domain.Transaction;

public interface TransactionRepository  extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

    Optional<Transaction> findByRequestIdAndType(String requestId, String type);
    
    
    Optional<Transaction> findByAccountIdAndRequestIdAndType(Long accountId, String requestId, String type);


}
