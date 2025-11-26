package com.fintech.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fintech.api.domain.DepositProduct;
import com.fintech.api.domain.ProductLike;
import com.fintech.api.domain.User;
import com.fintech.api.dto.ProductLikeResponseDto;
import com.fintech.api.repository.DepositProductRepository;
import com.fintech.api.repository.ProductLikeRepository;
import com.fintech.api.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;
    private final UserRepository userRepository;
    private final DepositProductRepository productRepository;

    // 추천하기
    @Transactional
    public void likeProduct(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        DepositProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        // 중복 추천 방지
        if (productLikeRepository.existsByUserAndProduct(user, product)) {
            throw new IllegalStateException("이미 추천한 상품입니다.");
        }

        productLikeRepository.save(ProductLike.of(user, product));
    }

    // 추천 취소
    @Transactional
    public void unlikeProduct(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        DepositProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        ProductLike like = productLikeRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new IllegalStateException("추천 기록 없음"));

        productLikeRepository.delete(like);
    }

    // 추천 수 조회
    public long getLikeCount(Long productId) {
        DepositProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));
        return productLikeRepository.countByProduct(product);
    }


     // 특정 상품을 추천한 사용자 목록을 조회
      public List<ProductLikeResponseDto> getUsersByProduct(Long productId) {
        DepositProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));
        
        List<ProductLike> likes = productLikeRepository.findByProduct(product);
        return likes.stream()
        .map(like -> ProductLikeResponseDto.builder()
                .userId(like.getUser().getId())
                .username(like.getUser().getName()) 
                .email(like.getUser().getEmail())
                .build()
        )
        .toList();



  
    }
}
