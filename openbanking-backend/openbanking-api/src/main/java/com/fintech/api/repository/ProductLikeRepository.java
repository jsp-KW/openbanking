package com.fintech.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintech.api.domain.DepositProduct;
import com.fintech.api.domain.ProductLike;
import com.fintech.api.domain.User;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
    boolean existsByUserAndProduct (User user, DepositProduct product); // 중복방지를 위한 이미 추천했는지 체크
    Optional<ProductLike> findByUserAndProduct (User user, DepositProduct product); // 추천 취소하는 경우 사용
    long countByProduct(DepositProduct product);
    List<ProductLike> findByProduct(DepositProduct product);
}
