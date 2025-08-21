
// Spring data JPA 가 구현체를 자동 생성
// Interface 라 유연성이 높아짐
// 구현체 커스터 마이징 가능
// Spring 이 runtime에 내부적으로 SimpleJpaRepository 등의 구현체를 자동 생성해서 DI 로 넣어줌
// SQL , 구현을 안해도 CRUD 기능이 자동으로 제공됨

// 엔티티 -> DB 구조 및 데이터 정의
// 리포지토리 -> 데이터 접근 책임만 담당
// 서비스 -> 비즈니스 로직
// 컨트롤러 -> 외부 요청 응답
// 관리 테스트 유지보수가 쉬워지는 패턴

// 리포지토리가 필요한 이유
// DB 접근 추상화 -> CRUD 동작을 메서드 하나로 처리
// 코드 양 감소 -> sql 안쓰고 쿼리 생성 가능
// 유지보수 쉬움 -> 구조화된 패턴 구성-> 확장되어도 안정적

package com.fintech.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import com.fintech.api.domain.Account;
import com.fintech.api.domain.User;

import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface AccountRepository extends JpaRepository<Account, Long> {


  Optional <Account> findByAccountNumber(String accountNumber);
    List<Account> findByUserId(Long userId);
   // List<Account> findByEmail(String email);
   Optional<Account> findByAccountNumberAndBankId(String accountNumber,Long bankId);

   boolean existsByAccountNumber(String accountNumber);
   int countByuserId(Long id);
   boolean existsByUserIdAndBankIdAndAccountType(Long id, Long id2, String accountType);

   // 비관적 쓰기락을 위한
   // 비관적 락은 Shared Lock(읽기 잠금), Exclusive Lock(쓰기 잠금)이 있음
   // 비관적 락은 데이터의 대해 접근이 엄격하고, 데이터 충돌이 많을거 같은 상황에서 많이 사용
   // 시스템 성능에 부정적 영향이 미칠 수 있음-> 같은 데이터를 수정하기 위해 다른 트랜잭션에서 가지고 있는 락을 획득하기 위해 대기시간이 길어질 수 있음
   // 대기시간 길어지면, 성능 저하 문제, 여러 트랜잭션이 서로 여러 데이터 요청 -> 데드락 
   
   // Hibernate가 메서드 호출시 , 쿼리를 날려 베타락을 건다
   // lock.timeout은 대기 시간, 초 단위로 동작하는 벤더가 많아서 운영 db 설정도 함께 고려
     // 베타락, 다른 트랜잭션에서 read,update,delete 되는 것을 방지(읽기만 가능)
   
    // findById는 단순 조회이기 때문에 락이 안걸림 -> 다른 트랜잭션이 동시에 같은 레코드 수정이 가능
    // 그러므로 @Lock(LockModeType.PESSIMISTIC_WRITE)을 붙여서 메서드 작성
    // 두 개의 입금계좌, 출금계좌를 일정한 순서로 잠그기 위해 ID기준으로 재조회할때 사용
    // 데드락 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    @QueryHints(@QueryHint(name= "jakarta.persistence.lock.timeout", value="5000"))
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

}
