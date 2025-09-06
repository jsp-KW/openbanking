package com.fintech.api.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.api.dto.DepositProductRequestDto;
import com.fintech.api.dto.DepositProductResponseDto;
import com.fintech.api.dto.ErrorResponseDto;
import com.fintech.api.dto.InterestSimulationRequestDto;
import com.fintech.api.dto.InterestSimulationResponseDto;
import com.fintech.api.dto.UserDepositRequestDto;
import com.fintech.api.dto.UserDepositResponseDto;
import com.fintech.api.service.DepositService;
import com.fintech.api.service.InterestCalculationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "예금 상품", description = "예금 상품 관련 API")
public class DepositController {

    private final DepositService depositService;
    private final InterestCalculationService interestCalculationService;

    // 상품 등록 (관리자 전용)
    @PostMapping("/product")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "상품 등록", description = "새로운 예금 상품을 등록합니다")
    @ApiResponse(responseCode = "201", description = "상품 등록 성공")
    public ResponseEntity<DepositProductResponseDto> createProduct(
            @RequestBody @Valid DepositProductRequestDto dto) {
        
        log.info("상품 등록 요청: {}", dto.getName());
        DepositProductResponseDto response = depositService.createProduct(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 전체 상품 조회
    @GetMapping("/products")
    @Operation(summary = "전체 상품 조회", description = "활성화된 모든 예금 상품을 조회합니다")
    public ResponseEntity<List<DepositProductResponseDto>> getAllProducts() {
        List<DepositProductResponseDto> products = depositService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // 예금상품 가입
    @PostMapping("/subscribe")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "예금상품 가입", description = "사용자가 예금상품에 가입합니다")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "409", description = "이미 가입한 상품"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<UserDepositResponseDto> subscribe(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UserDepositRequestDto requestDto) {

        log.info("사용자 {} 상품 가입 요청: 상품ID={}, 금액={}", 
                userDetails.getUsername(), requestDto.getProductId(), requestDto.getAmount());

        UserDepositResponseDto response = depositService.subscribeProduct(
                userDetails.getUsername(), requestDto
        );

        return ResponseEntity.ok(response);
    }

    // 내 예금상품 조회
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "내 예금상품 조회", description = "사용자가 가입한 예금상품 목록을 조회합니다")
    public ResponseEntity<List<UserDepositResponseDto>> getMyDeposits(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        List<UserDepositResponseDto> deposits = depositService.getMyDeposits(userDetails.getUsername());
        return ResponseEntity.ok(deposits);
    }

    //  현재 이자 조회
    @GetMapping("/my/{depositId}/interest")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "현재 이자 조회", description = "현재 시점까지의 이자를 계산하여 조회합니다")
    public ResponseEntity<BigDecimal> getCurrentInterest(
            @PathVariable Long depositId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("사용자 {} 현재 이자 조회 요청: depositId={}", userDetails.getUsername(), depositId);
        
        BigDecimal currentInterest = depositService.calculateCurrentInterest(
                depositId, userDetails.getUsername()
        );
        
        return ResponseEntity.ok(currentInterest);
    }

    //  조기 해지
    @DeleteMapping("/my/{depositId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "조기 해지", description = "예금을 조기 해지합니다 (수수료 적용)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조기 해지 성공"),
        @ApiResponse(responseCode = "400", description = "이미 처리된 예금"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<UserDepositResponseDto> earlyTermination(
            @PathVariable Long depositId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("사용자 {} 조기 해지 요청: depositId={}", userDetails.getUsername(), depositId);
        
        UserDepositResponseDto response = depositService.earlyTermination(
                depositId, userDetails.getUsername()
        );
        
        return ResponseEntity.ok(response);
    }

    // 이자 시뮬레이션
    @PostMapping("/simulate")
    @Operation(summary = "이자 시뮬레이션", description = "가입 전 예상 이자를 미리 계산합니다")
    public ResponseEntity<InterestSimulationResponseDto> simulateInterest(
            @RequestBody @Valid InterestSimulationRequestDto requestDto) {
        
        log.info("이자 시뮬레이션 요청: 금액={}, 이율={}, 기간={}개월", 
                requestDto.getAmount(), requestDto.getInterestRate(), requestDto.getPeriodMonths());
        
        // 단리와 복리 둘 다 계산해서 비교 제공
        BigDecimal simpleInterest = interestCalculationService.simulateInterest(
                requestDto.getAmount(),
                requestDto.getInterestRate(),
                requestDto.getPeriodMonths(),
                com.fintech.api.domain.InterestType.SIMPLE
        );
        
        BigDecimal compoundInterest = interestCalculationService.simulateInterest(
                requestDto.getAmount(),
                requestDto.getInterestRate(),
                requestDto.getPeriodMonths(),
                com.fintech.api.domain.InterestType.COMPOUND
        );
        
        InterestSimulationResponseDto response = InterestSimulationResponseDto.builder()
                .amount(requestDto.getAmount())
                .interestRate(requestDto.getInterestRate())
                .periodMonths(requestDto.getPeriodMonths())
                .simpleInterest(simpleInterest)
                .compoundInterest(compoundInterest)
                .totalAmountWithSimple(requestDto.getAmount().add(simpleInterest))
                .totalAmountWithCompound(requestDto.getAmount().add(compoundInterest))
                .interestDifference(compoundInterest.subtract(simpleInterest))
                .build();
        
        return ResponseEntity.ok(response);
    }

    // 만기 처리 (관리자 전용)
    @PostMapping("/process-maturity")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "만기 처리", description = "만료된 예금들을 일괄 처리합니다")
    public ResponseEntity<String> processMaturity() {
        log.info("관리자 만기 처리 요청");
        
        depositService.processMaturity();
        
        return ResponseEntity.ok("만기 처리가 완료되었습니다.");
    }

    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("INVALID_REQUEST")
                .message(e.getMessage())
                .build();
        
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponseDto> handleSecurity(SecurityException e) {
        log.warn("권한 오류: {}", e.getMessage());
        
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("ACCESS_DENIED")
                .message(e.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneral(Exception e) {
        log.error("서버 오류: {}", e.getMessage(), e);
        
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("서버 내부 오류가 발생했습니다.")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}