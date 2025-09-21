package com.fintech.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.api.domain.Account;
import com.fintech.api.domain.DepositProduct;
import com.fintech.api.domain.DepositStatus;
import com.fintech.api.domain.InterestType;
import com.fintech.api.domain.User;
import com.fintech.api.domain.UserDeposit;
import com.fintech.api.dto.DepositProductRequestDto;
import com.fintech.api.dto.DepositProductResponseDto;
import com.fintech.api.dto.UserDepositRequestDto;
import com.fintech.api.dto.UserDepositResponseDto;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.DepositProductRepository;
import com.fintech.api.repository.UserDepositRepository;
import com.fintech.api.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositService {

    private final DepositProductRepository productRepository;
    private final UserDepositRepository userDepositRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final InterestCalculationService interestCalculationService; 

    // 상품 등록 (관리자)
    public DepositProductResponseDto createProduct(DepositProductRequestDto dto) {
        DepositProduct product = DepositProduct.builder()
                .name(dto.getName())
                .interestRate(dto.getInterestRate())
                .interestType(dto.getInterestType() != null ? dto.getInterestType() : InterestType.SIMPLE) 
                .periodMonths(dto.getPeriodMonths())
                .minAmount(dto.getMinAmount())
                .type(dto.getType())
                .build();

        log.info("새로운 예금 상품 등록: {} (이자타입: {})", product.getName(), product.getInterestType());
        return DepositProductResponseDto.from(productRepository.save(product));
    }

    // 전체 상품 조회
    public List<DepositProductResponseDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(DepositProductResponseDto::from)
                .toList();
    }

    // 고객이 상품 가입
    @Transactional
    public UserDepositResponseDto subscribeProduct(String email, UserDepositRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 유저"));

        DepositProduct product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 중복 가입 체크 추가
        boolean alreadySubscribed = userDepositRepository.existsByUserAndProductAndStatus(
                user, product, DepositStatus.ACTIVE);
        
        if (alreadySubscribed) {
            throw new IllegalArgumentException("이미 가입한 상품입니다.");
        }

        // 출금 계좌 확인
        Account account = accountRepository.findByAccountNumber(dto.getFromAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("출금 계좌가 존재하지 않습니다."));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 계좌로만 가입할 수 있습니다.");
        }

        // 최소 금액 검증
        if (dto.getAmount().compareTo(product.getMinAmount()) < 0) {
            throw new IllegalArgumentException("가입 시 최소 금액에 미달합니다.");
        }

        // 계좌 잔액 검증
        if (BigDecimal.valueOf(account.getBalance()).compareTo(dto.getAmount()) < 0) {
            throw new IllegalArgumentException("계좌 잔액 부족");
        }

        // 계좌 잔액 차감
        account.setBalance(BigDecimal.valueOf(account.getBalance()).subtract(dto.getAmount()).longValue());

        // 가입일, 만기일 계산
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMonths(product.getPeriodMonths());

        //  예상 이자 미리 계산 (만기까지 일수 기준)
        long daysToMaturity = ChronoUnit.DAYS.between(startTime, endTime);
        BigDecimal expectedInterest = interestCalculationService.calculateInterest(
                dto.getAmount(),
                product.getInterestRate(),
                (int) daysToMaturity,
                product.getInterestType()
        );

        // UserDeposit
        UserDeposit userDeposit = UserDeposit.builder()
                .user(user)
                .product(product)
                .account(account)
                .amount(dto.getAmount())
                .joinedAt(startTime)
                .maturityDate(endTime)
                .status(DepositStatus.ACTIVE) //  새로운 enum 사용
                .interestAmount(expectedInterest) //  예상 이자로 설정
                .build();

        UserDeposit saved = userDepositRepository.save(userDeposit);

        log.info("사용자 {} 상품 {} 가입 완료 (금액: {}, 예상이자: {})", 
                 user.getEmail(), product.getName(), dto.getAmount(), expectedInterest);

        return UserDepositResponseDto.from(saved);
    }

    // 나의 예금상품 조회하기
    public List<UserDepositResponseDto> getMyDeposits(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않은 유저")
        );

        return userDepositRepository.findByUserId(user.getId()).stream()
                .map(UserDepositResponseDto::from).toList();
    }

    //  현재 이자 계산 (중도에 확인할 때)
    public BigDecimal calculateCurrentInterest(Long userDepositId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 유저"));

        UserDeposit userDeposit = userDepositRepository.findById(userDepositId)
                .orElseThrow(() -> new IllegalArgumentException("예금 정보를 찾을 수 없습니다."));

        if (!userDeposit.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인의 예금 정보만 조회할 수 있습니다.");
        }

        if (userDeposit.getStatus() != DepositStatus.ACTIVE) {
            return userDeposit.getInterestAmount(); // 이미 확정된 이자
        }

        // 현재까지의 일수 계산
        long daysPassed = ChronoUnit.DAYS.between(userDeposit.getJoinedAt(), LocalDateTime.now());
        
        return interestCalculationService.calculateInterest(
                userDeposit.getAmount(),
                userDeposit.getProduct().getInterestRate(),
                (int) daysPassed,
                userDeposit.getProduct().getInterestType()
        );
    }

    // 만기시 처리 배치 or 수동
    @Transactional
    public void processMaturity() {
        LocalDateTime now = LocalDateTime.now();

      
        List<UserDeposit> matured = userDepositRepository.findByStatusAndMaturityDateBefore(
                DepositStatus.ACTIVE, now
        );

        for (UserDeposit deposit : matured) {
            processIndividualMaturity(deposit);
        }

        log.info("만기 처리 완료: {}건", matured.size());
    }

    //  개별 만기 처리 로직 분리
    private void processIndividualMaturity(UserDeposit deposit) {
        DepositProduct product = deposit.getProduct();
        Account account = deposit.getAccount();

        //  새로운 이자 계산 로직 사용
        long actualDays = ChronoUnit.DAYS.between(deposit.getJoinedAt(), deposit.getMaturityDate());
        BigDecimal actualInterest = interestCalculationService.calculateInterest(
                deposit.getAmount(),
                product.getInterestRate(),
                (int) actualDays,
                product.getInterestType()
        );

        // 원금 + 이자를 계좌에 입금
        BigDecimal totalAmount = deposit.getAmount().add(actualInterest);
        account.setBalance(account.getBalance() + totalAmount.longValue());

        // 예금 상태 업데이트
        deposit.setInterestAmount(actualInterest);
        deposit.setStatus(DepositStatus.MATURED);

        log.info("예금 만기 처리: 사용자={}, 원금={}, 이자={}, 총액={}", 
                 deposit.getUser().getEmail(), 
                 deposit.getAmount(), 
                 actualInterest, 
                 totalAmount);
    }

    //  조기 해지 기능 추가
    @Transactional
    public UserDepositResponseDto earlyTermination(Long userDepositId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 유저"));

        UserDeposit userDeposit = userDepositRepository.findById(userDepositId)
                .orElseThrow(() -> new IllegalArgumentException("예금 정보를 찾을 수 없습니다."));

        if (!userDeposit.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인의 예금만 해지할 수 있습니다.");
        }

        if (userDeposit.getStatus() != DepositStatus.ACTIVE) {
            throw new IllegalArgumentException("이미 처리된 예금입니다.");
        }

        // 현재까지의 이자 계산 (조기해지 시 단리 적용)
        long daysPassed = ChronoUnit.DAYS.between(userDeposit.getJoinedAt(), LocalDateTime.now());
        BigDecimal earlyInterest = interestCalculationService.calculateInterest(
                userDeposit.getAmount(),
                userDeposit.getProduct().getInterestRate(),
                (int) daysPassed,
                InterestType.SIMPLE // 조기해지는 단리로만
        );

        // 조기해지 수수료 적용 (예: 이자의 50% 차감)
        BigDecimal penaltyRate = BigDecimal.valueOf(0.5);
        BigDecimal finalInterest = earlyInterest.multiply(penaltyRate);

        // 계좌에 원금 + 차감된 이자 입금
        Account account = userDeposit.getAccount();
        BigDecimal totalAmount = userDeposit.getAmount().add(finalInterest);
        account.setBalance(account.getBalance() + totalAmount.longValue());

        // 예금 상태 업데이트
        userDeposit.setInterestAmount(finalInterest);
        userDeposit.setStatus(DepositStatus.EARLY_TERMINATED);

        log.info("조기해지 처리: 사용자={}, 원금={}, 차감된이자={}", 
                 user.getEmail(), userDeposit.getAmount(), finalInterest);

        return UserDepositResponseDto.from(userDeposit);
    }
}