
package com.fintech.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fintech.api.domain.Transaction;

public interface TransactionRepository  extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

    // unique key (request_id, type) 에 따른 전역 조회 메서드를 추가
    Optional<Transaction> findByRequestIdAndType(String requestId, String type);
    
    
    Optional<Transaction> findByAccountIdAndRequestIdAndType(Long accountId, String requestId, String type);
   

    @Query("""
    select t from Transaction t
    join fetch t.account a
    where a.id = :accountId
      and a.user.email = :email
    order by t.transactionDate desc
""")
List<Transaction> findAllByAccountIdWithAccount(
    @Param("email") String email,
    @Param("accountId") Long accountId
);


}
