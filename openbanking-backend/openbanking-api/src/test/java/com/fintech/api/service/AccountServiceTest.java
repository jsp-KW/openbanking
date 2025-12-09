package com.fintech.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fintech.api.domain.Account;
import com.fintech.api.domain.AccountType;
import com.fintech.api.domain.Bank;
import com.fintech.api.domain.Transaction;
import com.fintech.api.domain.User;
import com.fintech.api.dto.AccountRequestDto;
import com.fintech.api.repository.AccountRepository;
import com.fintech.api.repository.BankRepository;
import com.fintech.api.repository.TransactionRepository;
import com.fintech.api.repository.UserRepository;

/*테스트 코드 작성 기본원칙
 * 1. 테스트 코드는 반복 가능해야함 -> 동일한 입력에 대한 항상 동일한 결과를 반환
 * 2. 테스트 코드는 명확하고 이해가 쉬워야함 -> 코드의 동작을 검증하는 역할이기 때문
 * 3. 테스트 코드는 빠르게 실행되어야함 -> 느리면 개발 속도가 저하될 수 있기 때문
 * 4. 테스트 코드는 지속적으로 유지보수 되어야함. -> 코드 변경-> 테스트코드도 업데이트
 * 
 * 
 * 단위 테스트 / 통합 테스트
 * 테스트 주도 개발 (Test Driven Development ,TDD)
 * TDD란, 테스트 코드를 먼저 작성한 후, 그 테스트를 통과하기 위한 코드를 작성하는 개발 방법론
 * 
 * TDD 기본 사이클
 * 1. 먼저 실패하는 테스트 코드 작성
 * 2. 테스트를 통과하기 위한 최소한의 코드 작성
 * 3. 작성한 코드를 리팩토링하여 품질을 높임
 * 
 * 테스트 코드 왜 중요한가?
 * -> 애플리케이션의 기능이 올바르게 동작하는지 확인하고, 버그를 조기에 발견하여 코드의 유지보수성을 높이는데 기여하기 때문
 * 유닛 테스트와 통합 테스트를 적절히 조합하여 사용하고, 테스트 주도 개발을 통해 코드의 품질을 높일 수 있으며 안정성도 확보가 가능
 */
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    
    @Mock // 가짜 객체를 만들음 테스트용 -> db나 외부 시스템 없이도 로직 테스트 가능하게 
    private AccountRepository accountRepository;

    @Mock // 이 함수가 값을 리턴하다로 미리 세팅해서 가짜처럼 굴리는것
    private UserRepository userRepository;

    @Mock
    private BankRepository bankRepository;
         
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PasswordEncoder passwordEncoder;


    @InjectMocks // test 하려는 진짜 클래스를 만들고, 내부 필요한 의존성(@Mocks)를 자동으로 주입
    private AccountService accountService;
   


    @Test
    void testCreateAccount() {
    
        String email = "user@example.com";

        User mockUser = User.builder().id(1L).email(email).build();// 빌더 패턴으로 객체 생성

        List <Account> mockAccounts = List.of(
            Account.builder().id(1L).accountNumber("12345").balance(1000L).build(),
            Account.builder().id(2L).accountNumber("789-012").balance(50000L).build()
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));// findByEmail이 호출되면, mockUser를 리턴해줘
        when(accountRepository.findByUserId(1L)).thenReturn(mockAccounts);// findByUserId 가 호출되면 mockAccount를 리턴해줘

        // when
        List<Account> result = accountService.getAccountsByEmail(email);

        // then
        assertEquals(2, result.size());// 기대한 값 2, 실제값 -> 같아야 테스트 통과, 틀리면 테스트 실패
        assertEquals("12345", result.get(0).getAccountNumber()); 
    
    }

    @Test
    void 특정_계좌삭제 () {
        String email1= "user@example.com";

        User user = User.builder().id(1L).email(email1).build(); //user id가 1이고 이메일이 email1인 유저를 만든다

        //계좌 아이디가 1이고, 계좌번호가 12345 이며, 잔액이 1000인 유저의 계좌를 만든다
        // .user() 가 가능한 이유는 user 엔티티하고, account 엔티티에서 user_id pk를 fk로 참조하고 있기 때문
        Account account =Account.builder().id(1L).accountNumber("12345").balance(1000L).user(user).build();
         
        //계좌 아이디가 1인 함수 findById가 호출되면, 계좌를 리턴해라
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        //검증하고 싶은 함수 deleteAccountWithAuth 호출
        accountService.deleteAccountWithAuth(1L,email1);

        verify(accountRepository).delete(account); // accountRepository.delete()가 호출되었는지 체크하는 코드
    }



     
    
    // 일반적인 이체가 가능한지 test
    @Test 
    void test_이체() {
        String email= "test@example.com";
        User user =  User.builder().id(1L).email(email).build();
        Bank fromBank = Bank.builder().id(1L).code("001").build();
        Bank toBank = Bank.builder().id(2L).code("002").build();


    
        Account fromAccount = Account.builder().id(1L).accountNumber("1234").accountType(AccountType.CHECKING).bank(fromBank).balance(1000L).user(user).accountPassword("encodedPw").build();

        User otherUser = User.builder().id(2L).email("other@example.com").build();
        Account toAccount = Account.builder().id(2L).accountNumber("456").accountType(AccountType.CHECKING).balance(1000L).bank(toBank).user(otherUser).accountPassword("encodedPw").build();
    
        when(accountRepository.findByAccountNumberAndBankId("1234", 1L)).thenReturn(
        (Optional.of(fromAccount)));
        

        
        when (accountRepository.findByAccountNumberAndBankId("456", 2L)).thenReturn(
        (Optional.of(toAccount))
        );
        
        when (passwordEncoder.matches("1234", "encodedPw")).thenReturn(true);

        accountService.transfer(email,1L,2L,"1234","456", 500L,"1234", "req-001");
        assertEquals(500L, fromAccount.getBalance());
        assertEquals(1500L, toAccount.getBalance());


        verify(transactionRepository,times(2)).save(any(Transaction.class));

        verify(notificationService,atLeast(2)).createNotification(any());
    }

    // 잔액이 부족한 경우 이체가 불가능
    @Test
    void 잔액부족_이체실패() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).build();

        Bank fromBank = Bank.builder().id(1L).code("001").build();
        Bank toBank = Bank.builder().id(2L).code("002").build();

        Account fromAccount = Account.builder().id(1L).accountNumber("1234").balance(100L).user(user).bank(fromBank).accountPassword("encodedPw").build();  // 100원밖에 없음
        Account toAccount = Account.builder().id(2L).accountNumber("5678").balance(1000L).user(user).bank(toBank).accountPassword("encodedPw").build();

        when(accountRepository.findByAccountNumberAndBankId("1234", 1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumberAndBankId("5678", 2L)).thenReturn(Optional.of(toAccount));
        when(passwordEncoder.matches("1234", "encodedPw")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            accountService.transfer(email, 1L, 2L, "1234", "5678", 1000L, "1234", "req-002");  // 1000원 이체 시도
        });

    }

    // 입금계좌가 명확하지 않는 경우 이체가 실패되어야함 -> 입금계좌가 없는 경우
    @Test 
    void 입금계좌없어_이체실패() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).build();

        Bank fromBank = Bank.builder().id(1L).code("001").build();
        
        Account fromAccount = Account.builder().id(1L).accountNumber("1234").balance(10000L).bank(fromBank)
        .user(user).accountPassword("encodedPw").build();

        when(accountRepository.findByAccountNumberAndBankId("1234", 1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumberAndBankId("9999", 99L)).thenReturn(Optional.empty());
        when(passwordEncoder.matches("1234", "encodedPw")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, ()-> {
            accountService.transfer(email, 1L, 99L, "1234", "9999", 1000L, "1234", "req-003");
        });
    }

    // 동일한 계좌로 이체가 실패되어야하기에
    @Test
    void 동일계좌이체_실패() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).build();
        Bank bank = Bank.builder().id(1L).code("001").build();

        Account account = Account.builder().id(1L).accountNumber("1234").balance(5000L).user(user).bank(bank).accountPassword("encodedPw").build();

        when(accountRepository.findByAccountNumberAndBankId("1234", 1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("1234", "encodedPw")).thenReturn(true);

       Exception exception= assertThrows(IllegalArgumentException.class, () -> {
            accountService.transfer(email, 1L, 1L, "1234", "1234", 1000L,"1234","req-004");
        });

        assertEquals("동일한 계좌로는 이체가 불가능합니다.", exception.getMessage());
    }

    // 타인 계좌는 삭제하면 안됨
    
    @Test
    void 타인의계좌_삭제_실패() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).build();
        User otherUser = User.builder().id(2L).email("other@example.com").build();

        Account account = Account.builder().id(1L).accountNumber("9999").user(otherUser).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Exception ex = assertThrows(AccessDeniedException.class, () -> {
            accountService.deleteAccountWithAuth(1L, email);
        });

        assertEquals("본인 계좌만 삭제 가능", ex.getMessage()); 
    }

    @Test
    void 존재하지않는계좌_삭제실패() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            accountService.deleteAccountWithAuth(999L, "test@example.com");
        });

        assertEquals("계좌 없음", ex.getMessage());  // 예외처리구문의 내용과 동일하게 떠야함
    }

    @Test 
    void 계좌가0개일때_빈리스트_반환하는가() {
        String email = "user@example.com";

        User user = User.builder().id(1L).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(user.getId())).thenReturn(List.of());

        List <Account> result = accountService.getAccountsByEmail(email);

        assertEquals(0, result.size()); // 리스트 사이즈가 0 이랑 같아야함
    }




    @Test
    void 계좌중복없이_랜덤으로_생성이_잘되는지() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).build();
        Bank bank = Bank.builder().id(1L).code("001").build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bankRepository.findById(1L)).thenReturn(Optional.of(bank));

        when(accountRepository.existsByAccountNumber(any())).thenReturn(true)
        .thenReturn(false);
        // accountRepository.save()가 호출되면, getArgument(0) 맨 처음을 return 해줘
        when (accountRepository.save(any(Account.class))).thenAnswer(t->t.getArgument(0));

        AccountRequestDto dto = new AccountRequestDto();
        dto.setBankId(1L);
        dto.setBalance(1000L);
        dto.setAccountType("예적금");

        accountService.createAccount(dto, email); // 서비스 로직 실행

        // 두번의 계좌생성로직함수 조건문의 조건인 existsByAccountNumber 의 실행 횟수가 2번인지 검증
        // 처음은 중복이 된다라고 설정, 두번째부터는 중복x 생성이 된걸로 테스트 시나리오를 짬
        // 즉 이렇게 되면 2번 호출되어야한다는 뜻
        verify(accountRepository,atLeast(2)).existsByAccountNumber(any());

    }
    
    @Test
    void 동일유형계좌중복_생성_실패() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).build();
        Bank bank = Bank.builder().id(1L).code("001").build();

        AccountRequestDto dto = new AccountRequestDto();
        dto.setBankId(1L);
        dto.setBalance(1000L);
        dto.setAccountType("예적금");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        when(bankRepository.findById(1L)).thenReturn(Optional.of(bank));
        
        when(accountRepository.existsByUserIdAndBankIdAndAccountType(
                user.getId(), bank.getId(), AccountType.fromLabel(dto.getAccountType())))
            .thenReturn(true); // 중복 계좌가 이미 존재한다고 가정하고

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount(dto, email);
        });

        assertEquals("동일한 유형과 같은 계좌번호를 가진 계좌가 이미 존재합니다.", ex.getMessage()); // 예외처리구문 내용과 동일하게 나와야함
    }
    // 계좌 10개 있다고 가정후 하나 추가한다는 시나리오
    @Test
    void 계좌_10개초과시_생성이_실패() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).build();
        Bank bank = Bank.builder().id(1L).code("001").build();

        // 계좌가 10개 이미 있다고 설정해놓고 테스트해보기
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bankRepository.findById(1L)).thenReturn(Optional.of(bank));
        when(accountRepository.countByuserId(user.getId())).thenReturn(10);

        AccountRequestDto dto = new AccountRequestDto();

        dto.setBankId(1L);
        dto.setBalance(1000L);
        dto.setAccountType("예적금");

        Exception ex = assertThrows(IllegalStateException.class, () -> {
            accountService.createAccount(dto, email);
        });

        assertEquals("계좌는 최대 10개까지 개설이 가능합니다.", ex.getMessage());
        }

    // 초기 잔액 음수인 경우 계좌생성 실패하는 예외처리구문이 잘 돌아가는지    
    @Test
    void 초기잔액_음수시_계좌생성_실패() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).build();
        Bank bank = Bank.builder().id(1L).code("001").build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bankRepository.findById(1L)).thenReturn(Optional.of(bank));
    

        AccountRequestDto dto = new AccountRequestDto();
        dto.setBankId(1L);
        dto.setBalance(-1000L); 
        dto.setAccountType("예적금");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount(dto, email);
        });

        assertEquals("초기 잔액은 음수일 수 없습니다.", ex.getMessage());
    }
}

