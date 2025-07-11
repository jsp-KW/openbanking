
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

import org.springframework.data.jpa.repository.JpaRepository;
import com.fintech.api.domain.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);
   // List<Account> findByEmail(String email);
}
