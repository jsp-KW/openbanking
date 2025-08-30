package com.fintech.api.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fintech.api.domain.Account;
import com.fintech.api.domain.NotificationType;
import com.fintech.api.domain.ScheduledTransfer;
import com.fintech.api.domain.Transaction;
import com.fintech.api.domain.User;
import com.fintech.api.dto.CreateNotificationRequestDto;
import com.fintech.api.dto.CreateScheduledTransferRequestDto;
import com.fintech.api.dto.ScheduledTransferListResponseDto;
import com.fintech.api.dto.ScheduledTransferResponseDto;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.ScheduledTransferRepository;
import com.fintech.api.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduledTransferService {

    private final TransactionRepository transactionRepository;
    private final ScheduledTransferRepository scheduledTransferRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     *  예약이체 등록 
     *  CreateScheduledTransferRequestDto 를 인자로
     */
    @Transactional
    public ScheduledTransferResponseDto createScheduledTransfer(User user, CreateScheduledTransferRequestDto dto, String requestId) {
        
        System.out.println("dto 확인"+ dto.getFromAccountNumber());
        System.out.println("출금계좌 확인" + dto.getToAccountNumber());
        Account from = accountRepository.findByAccountNumber(dto.getFromAccountNumber()).orElseThrow(() -> new IllegalArgumentException("출금 계좌 없습니다.")); //출금 계좌 엔티티 객체

        if (from.getAccountPassword() == null || !passwordEncoder.matches(dto.getPassword(),from.getAccountPassword())){
            throw new SecurityException("계좌 비밀번호 불일치");
        }

        if (!from.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인의 계좌에서만 예약이체 등록이 가능합니다.");
        }
        // 예약이체 받을 계좌 은행id, 계좌번호
        Account to = accountRepository.findByAccountNumberAndBankId(
            dto.getToAccountNumber(),
            dto.getToBankId()
        ).orElseThrow(() -> new IllegalArgumentException("입금 계좌(은행 포함) 정보가 유효하지 않습니다."));

        // 예약이체 엔티티 초기화
        ScheduledTransfer st = ScheduledTransfer.builder().user(user).fromAccount(from)
                .toAccount(to).amount(dto.getAmount()).scheduledAt(dto.getScheduledAt())
                .status("대기").build();

        // 초기화된 엔티티 db 저장하고, 저장된 엔티티 반환된것 saved 변수에 저장
        ScheduledTransfer saved = scheduledTransferRepository.save(st);
        
        // from 함수를 통해 예약이체 등록 응답 dto 의 from 함수 호출
        // Entity->dto
        return ScheduledTransferResponseDto.from(saved);
    }

    /**
     *  예약이체 배치 처리
     *  추후에 실시간 대비를 위해 Kafka 도입 해보기
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void executeScheduledTransfers() {
        List<ScheduledTransfer> scheduleds = scheduledTransferRepository.findByStatusAndScheduledAtBefore("대기", LocalDateTime.now());

        for (ScheduledTransfer s : scheduleds) {
          try {
            processTransfer(s);
            s.setStatus("완료");
            scheduledTransferRepository.save(s);

            notificationService.createNotification(CreateNotificationRequestDto.builder().userId(s.getUser().getId())
                .message("예약 이체 완료: " + s.getAmount() + "원").type(NotificationType.SCHEDULED_TRANSFER).build());
          }
          
          catch (Exception e) {
            s.setStatus("실패");
            scheduledTransferRepository.save(s);
            notificationService.createNotification(CreateNotificationRequestDto.builder().userId(s.getUser().getId()).message("예약이체 실패: " + e.getMessage())
                .type(NotificationType.INSUFFICIENT_BALANCE).build()
            );
          }
        }
    }

    /**
     *  실제 잔액 이동 처리
     *  예약 이체후 잔액 변동을 적용하는 함수
     */
    private void processTransfer(ScheduledTransfer st) {
        Account from = st.getFromAccount();
        Account to = st.getToAccount();
        Long amount = st.getAmount();

        if (from.getBalance() < amount) throw new IllegalArgumentException("잔액 부족");

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);


        // 멱등키 백엔드기반 자동으로 생성
        String baseId = "SCHEDULED-" + st.getId();
        String requestIdOut = baseId + "-OUT";
        String requestIdIn = baseId + "-IN";

        // 예약이체 성공시 트랜잭션 기록 남기기 위해 추가하기!!

        transactionRepository.save(Transaction.builder().account(from).amount(-amount).type("출금")
        .description("예약이체 출금: " + to.getAccountNumber()).balanceAfter(from.getBalance()).requestId(requestIdOut).build()
        );

        
        transactionRepository.save(Transaction.builder().account(to).amount(amount).type("입금")
        .description("예약이체 입금: " +from.getAccountNumber()).balanceAfter(to.getBalance()).requestId(requestIdIn).build()
        );
    }

      /*
     *  나의 예약이체 내역 조회하는 함수
     */
    public List<ScheduledTransferListResponseDto> getMyScheduledTransfers(Long userId) {
        List<ScheduledTransfer> scheduledTransfers = scheduledTransferRepository.findByUserId(userId);

        return scheduledTransfers.stream().map(ScheduledTransferListResponseDto::from).toList();
    
    }


  

     
}