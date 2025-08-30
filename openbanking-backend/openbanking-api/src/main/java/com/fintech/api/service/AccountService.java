package com.fintech.api.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.fintech.api.domain.Account;
import com.fintech.api.domain.Bank;
import com.fintech.api.domain.NotificationType;
import com.fintech.api.domain.Transaction;
import com.fintech.api.domain.User;
import com.fintech.api.dto.AccountRequestDto;
import com.fintech.api.dto.CreateNotificationRequestDto;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.BankRepository;
import com.fintech.api.repository.TransactionRepository;
import com.fintech.api.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// 가독성 : 유저조회 설정, 저장
// 유지보수성 : 모든 계좌 생성 로직이 한 곳에서 관리
// 테스트 용이성
// 스프링 규칙 +통일성 -> 계층 분리, DI, 예외처리 

@Service
@RequiredArgsConstructor // spring DI  의존성 주입
public class AccountService {
    // JPA REPOSITORY 사용 -> 기본적인 CRUD 제공
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    // 계좌 번호 생성 로직!!
    private String makeAccountNumber(Bank bank) {
        String accountNumber;
        String bankCode = bank.getCode();
        do {
            long raw = ThreadLocalRandom.current().nextLong(10000000, 99999999);
            accountNumber = bankCode + "-" + raw;
        
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    // findById () JPA 기본 메서드
    // User 엔티티의 ID를 가져오는 함수

    // setUser 
    // Account 하고 User와 ManyToOne 관계로 묶여있어서
    // 계좌 생성시 어떤 사용자에게 소속되는지 명시해야 하므로

    // JPA 의 save()
    // INSERT / UPDATE 를 알아서 처리해줌
    // save 이후 자동으로 트랜잭션이 반영
    // 트랜잭션이 묶여있고 -> 내부적으로 영속성 컨텍스트에 의해 관리

    // Optional & 예외 처리 부분
    // null-safe 
  
    
    // 계좌 생성 (사용자와 연결)
    @Transactional  // db 쓰기 작업 -> 트랜잭션 보장 명시 
    public Account createAccount(AccountRequestDto dto, String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

    Bank bank = bankRepository.findById(dto.getBankId())
        .orElseThrow(() -> new IllegalArgumentException("은행 없음"));
    
 
    // 계좌 수 제한 10개로
    if (accountRepository.countByuserId(user.getId())>=10) {
        throw new IllegalStateException("계좌는 최대 10개까지 개설이 가능합니다.");
    } 
    // 초기 잔액 음수 방지하는 예외처리
    if (dto.getBalance()!=null && dto.getBalance()<0) {
        throw new IllegalArgumentException("초기 잔액은 음수일 수 없습니다.");
    }

    //계좌의 유효성 검사-> 이넘타입으로 변경 고려하기

    List<String> valid_accountType = List.of("예적금","입출금","청약");
    if (!valid_accountType.contains(dto.getAccountType())) {
        throw new IllegalArgumentException("유효하지 않은 계좌유형입니다.");
    }
 
   
    Account account = new Account();
    account.setUser(user);
    account.setBank(bank);
    account.setAccountNumber(makeAccountNumber(bank));
    account.setBalance(dto.getBalance() != null ? dto.getBalance() : 0L);
    account.setAccountType(dto.getAccountType());
    account.setAccountPassword(passwordEncoder.encode(dto.getPassword()));
    Account saved = accountRepository.save(account);

    notificationService.createNotification(
      CreateNotificationRequestDto.builder().userId(user.getId())
      .message("새로운 계좌를 개설하였습니다: " + saved.getAccountNumber()).type(NotificationType.ACCOUNT_CREATED).build()
    
    );

    return saved;

  
}
    // 특정 사용자의 모든 계좌 리스트 조회 함수
    public List<Account> getAccountsByEmail (String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        return accountRepository.findByUserId(user.getId());
        
    }
    // 특정 계좌 조회
    public Optional<Account> getAccountById (Long id) {
        return accountRepository.findById(id);
    }
    // 특정 계좌 삭제
    public void deleteAccount (Long id) {
        accountRepository.deleteById(id);
    }

    // 계좌 삭제 & 인가된 사용자만
    @Transactional
    public void deleteAccountWithAuth(Long accountId, String email) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("계좌 없음"));
        
            if (!account.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("본인 계좌만 삭제 가능");
        }
        
        accountRepository.delete(account);

        notificationService.createNotification(
                CreateNotificationRequestDto.builder()
                .userId(account.getUser().getId())
                .message(account.getAccountNumber() + " 계좌가 삭제되었습니다.")
                .type(NotificationType.SYSTEM_NOTICE)
                .build()
        );

    }
  
    public Optional<Account> getAccountByNumber(String accountNumber, Long bankId) {
    return accountRepository.findByAccountNumberAndBankId(accountNumber,bankId);
    }

    // 이체 입출금 서비스 로직 추가구현
    // 낙관락
    @Transactional
    public void transfer(String email, Long fromBankId, Long toBankId, String fromAccountNumber, String toAccountNumber, Long amount, String password, String requestId ) {


        // // 출금 중복 방지 예외처리

        // if (transactionRepository.findByRequestIdAndType(requestId,"출금").isPresent()) {
        //     throw new IllegalStateException("이미 처리된 출금 요청입니다.");
        // }

        // // 입금 중복 방지 예외처리

        // if (transactionRepository.findByRequestIdAndType(requestId, "입금").isPresent()) {
        //     throw new IllegalStateException("이미 처리된 입금 요청입니다.");
        // }



        Account from = accountRepository.findByAccountNumberAndBankId(fromAccountNumber,fromBankId).orElseThrow(()->
            new IllegalArgumentException("출금 계좌 또는 은행 정보가 유효하지 않습니다.")
        );

        Account to = accountRepository.findByAccountNumberAndBankId(toAccountNumber,toBankId).orElseThrow(()->
            new IllegalArgumentException("입금 계좌 또는 은행 정보가 유효하지않습니다")
    
        );

        if(!from.getUser().getEmail().equals(email)) {
            throw new SecurityException("본인의 계좌에서만 이체가 가능합니다.");
        }

        if (from.getId().equals(to.getId())) {
            throw new IllegalArgumentException("동일한 계좌로는 이체가 불가능합니다.");
        }


        if (from.getAccountPassword() == null) {
            throw new SecurityException("비밀번호가 설정되지 않은 계좌에서는 이체가 불가능합니다.");
        }

        if (!passwordEncoder.matches(password, from.getAccountPassword())) {
            throw new SecurityException("계좌 비밀번호가 일치하지 않습니다.");
        }

        Long fromBalance = from.getBalance() != null ? from.getBalance() : 0L;
        Long toBalance = to.getBalance() != null ? to.getBalance() :0L;

        if (fromBalance < amount) { // 현재 잔액이 부족한 경우
            notificationService.createNotification(CreateNotificationRequestDto.builder().userId(from.getUser().getId()
            ).message("잔액이 부족하여 이체가 실패하였습니다.").type(NotificationType.INSUFFICIENT_BALANCE).build()
            );

            throw new IllegalArgumentException("잔액이 부족하여 이체가 실패하였습니다.");
        }

        try {
            from.setBalance(fromBalance-amount);
            to.setBalance(toBalance + amount);

            Transaction withdrawTx = Transaction.builder()
            .account(from)
            .amount(-amount)
            .type("출금")
            .balanceAfter(from.getBalance())
            .description(to.getAccountNumber() + "으로 이체됨")
            .requestId(requestId)
            .build();

            Transaction depositTx = Transaction.builder()
            .account(to)
            .amount(amount)
            .type("입금")
            .balanceAfter(to.getBalance())
            .description(from.getAccountNumber() + "에서 입금됨")
            .requestId(requestId)
            .build();


            

            transactionRepository.save(withdrawTx);
            transactionRepository.save(depositTx);

            
                // 이체자 알람

            notificationService.createNotification(CreateNotificationRequestDto.builder().userId(from.getUser().getId())
                .message(amount+ "원이 "  + toAccountNumber + " 계좌로 이체 완료되었습니다.")
                .type (NotificationType.TRANSFER).build()
            
            );
            // 입금자 알람
            notificationService.createNotification(CreateNotificationRequestDto.builder().userId(to.getUser().getId())
                .message(from.getAccountNumber() + " 계좌에서 "  + amount + "원이 입금되었습니다.")
                .type (NotificationType.TRANSFER).build()
            
            );
            Long high_value_threshold =  1_000_000L; // 고액 임계값 변수 
            // 고액 기준이 넘는 돈을 이체하는 경우
            // 이체하는 사람에게 알람
            if (amount >= high_value_threshold) {
                notificationService.createNotification(CreateNotificationRequestDto.builder().userId(from.getUser().getId()).message("고액 거래 감지: " + amount +" 원이 이체되었습니다.")
                .type(NotificationType.HIGH_VALUE_TRANSACTION).build()
                );

                notificationService.createNotification(CreateNotificationRequestDto.builder().userId(to.getUser().getId()).message("고액 거래 감지: " + amount +" 원이 입금되었습니다.")
                .type(NotificationType.HIGH_VALUE_TRANSACTION).build()
                );
            }
        }
        catch (DataIntegrityViolationException dup) {
              // (request_id, type) 유니크 충돌 → 이미 처리된 요청. 이 트랜잭션은 롤백
              TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
              return;
        }
       
    }

    // 비관락 전용 서비스 로직 추가하기

    // 락 없이 id 파악 -> id 오름차순으로 findByIdForUpdate 함수로 베타락 -> from/to 매핑 -> 잔액처리

    @Transactional
    public void transferWithPessimisticlock(String email, Long fromBankId, Long toBankId, String fromAccountNumber, String toAccountNumber, Long amount, String password, String requestId)  {


        if (amount == null || amount <=0)  {throw new IllegalArgumentException("이체 금액이 올바르지 않습니다.");}

        // 잠금 없이 식별하기
        Account from0 = accountRepository.findByAccountNumberAndBankId(fromAccountNumber, fromBankId).orElseThrow(
            ()-> new IllegalArgumentException("출금 게좌 또는 은행 정보가 유효하지 않습니다.")
        );

        Account to0 = accountRepository.findByAccountNumberAndBankId(toAccountNumber, toBankId).orElseThrow(
            ()-> new IllegalArgumentException("입금 게좌 또는 은행 정보가 유효하지 않습니다.")
        );

        if (from0.getId().equals(to0.getId())) {
            throw new IllegalArgumentException("동일한 계좌로는 이체가 불가능합니다.");
        }

        // 데드락 방지

        // 상황 가정
        // 두명의 사용자가 동시에 이체한다
        // A가 B에게 이체, B가 A에게 이체 요청이 동시에 들어오는 상황
        // 비관적 락은 조회 순서대로 락을 가져가기 때문에 한쪽은 먼저 A계좌를 잠그고 B를 기다리고
        // 다른한쪽은 먼저 B계좌를 잠그고 A를 기다리는 교착상태(데드락) 발생 가능

        // 이 문제를 해결하기 위해
        // 항상 작은 ID -> 큰 ID 순서로 락을 거는 규칙을 만들면, 모든 트랜잭션이 같은 순서로 락을 잡기 때문에 데드락이 사라진다.
        // 즉, 락 순서를 고정시킴으로써 교착상태를 원천 차단하는 법
        // 오름차순을 사용함, 내림차순도 가능
        

        Long a = Math.min(from0.getId(), to0.getId());// 작은ID
        Long b = Math.max(from0.getId(), to0.getId());// 큰 ID

        // 락 작은 아이디 -> 큰 아이디 순서로 베타락 획득
        Account first = accountRepository.findByIdForUpdate(a).orElseThrow();
        Account second = accountRepository.findByIdForUpdate(b).orElseThrow();
       
        // 데드락 회피를 위해 id를 오름차순으로 조회하였고
        // 조회한 id와 원래 레코드 id와 다를 수 있기 때문에 매핑
        Account from = first.getId().equals(from0.getId()) ? first : second;
        Account to = first.getId().equals(to0.getId()) ? first : second;


        if (!from.getUser().getEmail().equals(email)) {
            throw new SecurityException("본인의 계좌에서만 이체가 가능합니다.");
        }

        if (from.getAccountPassword() == null) {
            throw new SecurityException("비밀번호가 설정되지 않은 계좌에서는 이체가 불가능합니다.");
        }

        if (!passwordEncoder.matches(password, from.getAccountPassword())) {
            throw new SecurityException("계좌 비밀번호가 일치하지 않습니다.");
        }

        long fromBalance = from.getBalance() == null? 0L : from.getBalance();
        long toBalance = to.getBalance() == null? 0L : to.getBalance();

        if (fromBalance <amount ) {
            notificationService.createNotification(CreateNotificationRequestDto.builder()
                .userId(from.getUser().getId())
                .message("잔액이 부족하여 이체가 실패하였습니다.")
                .type(NotificationType.INSUFFICIENT_BALANCE)
                .build());

            
            throw new IllegalArgumentException("잔액이 부족하여 이체가 실패하였습니다.");
        }

        try {
            from.setBalance(fromBalance-amount);
            to.setBalance(toBalance + amount);

            Transaction withdrawTx = Transaction.builder()
            .account(from)
            .amount(-amount)
            .type("출금")
            .balanceAfter(from.getBalance())
            .description(to.getAccountNumber() + "으로 이체됨")
            .requestId(requestId)
            .build();

            Transaction depositTx = Transaction.builder()
            .account(to)
            .amount(amount)
            .type("입금")
            .balanceAfter(to.getBalance())
            .description(from.getAccountNumber() + "에서 입금됨")
            .requestId(requestId)
            .build();


            

            transactionRepository.save(withdrawTx);
            transactionRepository.save(depositTx);

            
                // 이체자 알람

            notificationService.createNotification(CreateNotificationRequestDto.builder().userId(from.getUser().getId())
                .message(amount+ "원이 "  + toAccountNumber + " 계좌로 이체 완료되었습니다.")
                .type (NotificationType.TRANSFER).build()
            
            );
            // 입금자 알람
            notificationService.createNotification(CreateNotificationRequestDto.builder().userId(to.getUser().getId())
                .message(from.getAccountNumber() + " 계좌에서 "  + amount + "원이 입금되었습니다.")
                .type (NotificationType.TRANSFER).build()
            
            );
            Long high_value_threshold =  1_000_000L; // 고액 임계값 변수 
            // 고액 기준이 넘는 돈을 이체하는 경우
            // 이체하는 사람에게 알람
            if (amount >= high_value_threshold) {
                notificationService.createNotification(CreateNotificationRequestDto.builder().userId(from.getUser().getId()).message("고액 거래 감지: " + amount +" 원이 이체되었습니다.")
                .type(NotificationType.HIGH_VALUE_TRANSACTION).build()
                );

                notificationService.createNotification(CreateNotificationRequestDto.builder().userId(to.getUser().getId()).message("고액 거래 감지: " + amount +" 원이 입금되었습니다.")
                .type(NotificationType.HIGH_VALUE_TRANSACTION).build()
                );
            }
        }
        catch (DataIntegrityViolationException dup) {
              // (request_id, type) 유니크 충돌 → 이미 처리된 요청. 이 트랜잭션은 롤백
              TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
              return;
        }
      

    }

}
