package com.fintech.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.api.dto.ErrorResponseDto;
import com.fintech.api.dto.MessageResponse;
import com.fintech.api.dto.ProductLikeResponseDto;
import com.fintech.api.service.ProductLikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "상품 추천", description = "상품 추천/취소 및 추천 수 조회 API")
public class ProductLikeController {

    private final ProductLikeService productLikeService;

    // 상품 추천
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{productId}/like")
    @Operation(summary = "상품 추천", description = "로그인한 사용자가 특정 상품을 추천합니다")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "추천 성공"),
        @ApiResponse(responseCode = "409", description = "이미 추천한 상품"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<MessageResponse> likeProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("사용자 {} → 상품 추천 요청: productId={}", userDetails.getUsername(), productId);

        productLikeService.likeProduct(userDetails.getUsername(), productId);
        return ResponseEntity.ok(new MessageResponse("추천 성공"));// 추천 성공 메시지 json 반환
    }

    // 상품 추천 취소
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // jwt role 검사
    @SecurityRequirement(name = "bearerAuth") // swagger 문서 jwt 토큰 필요하다고 표시
    @DeleteMapping("/{productId}/like") // Restful api 설계
    @Operation(summary = "상품 추천 취소", description = "로그인한 사용자가 특정 상품에 대한 추천을 취소합니다")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "추천 취소 성공"),
        @ApiResponse(responseCode = "404", description = "추천하지 않은 상품"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<MessageResponse> unlikeProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("사용자 {} → 상품 추천 취소 요청: productId={}", userDetails.getUsername(), productId);

        productLikeService.unlikeProduct(userDetails.getUsername(), productId);
        return ResponseEntity.ok(new MessageResponse("추천 취소 성공"));// 추천 취소 성공 메시지 json 반환
    }

    // 특정 상품의 추천 사용자 목록 (관리자 전용)
    @PreAuthorize("hasRole('ADMIN')") // 관리자 권한 필요
    @GetMapping("/{productId}/likes/users")
    @Operation(summary = "추천 사용자 조회 (관리자 전용)", description = "해당 상품을 추천한 사용자 목록을 조회합니다")
    public ResponseEntity<List<ProductLikeResponseDto>> getUsersByProduct(@PathVariable Long productId) {
        List<ProductLikeResponseDto> users = productLikeService.getUsersByProduct(productId);
        return ResponseEntity.ok(users);
    }
    // 반환타입이 왜 저렇게 되는가 -> 나는 http code가 200 ok이고 , ProductLikeResponseDto 객체들이 배열 형태로 담긴 목록을 클라이언트에 응답할 것"

    // 상품 추천 수 조회 (비로그인도 가능)
    @GetMapping("/{productId}/likes")
    @Operation(summary = "상품 추천 수 조회", description = "특정 상품의 총 추천 수를 조회합니다")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long productId) {
        long count = productLikeService.getLikeCount(productId);
        return ResponseEntity.ok(count);
    }

   // ====== 공통 예외 처리 ======
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException e) {
    log.warn("잘못된 요청: {}", e.getMessage());
    return ResponseEntity.badRequest().body(

        ErrorResponseDto.builder().code("INVALID_REQUEST")
        .message(e.getMessage()).build()
    );
}

@ExceptionHandler(SecurityException.class)
public ResponseEntity<ErrorResponseDto> handleSecurity(SecurityException e) {
    log.warn("권한 오류: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponseDto.builder().code("AUTHORIZATION_FAILED")
            .message(e.getMessage()).build()
    );
}

@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponseDto> handleGeneral(Exception e) {
    log.error("서버 오류 발생", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponseDto.builder().code("INTERNAL_SERVER_ERROR")
            .message(e.getMessage()).build()
    );
}

}
