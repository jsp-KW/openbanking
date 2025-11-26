package com.fintech.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fintech.api.domain.DepositProduct;


public interface DepositProductRepository extends JpaRepository<DepositProduct, Long> {

 }


