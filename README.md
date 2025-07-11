# OpenBanking API

Spring Boot 기반의 오픈뱅킹 시스템입니다.  
실제 사용자 인증을 기반으로 계좌 개설, 조회, 이체, 거래내역 조회까지 가능한  
RESTful API 서버를 구현하고 있습니다.

---

## 주요 기능 요약

### 사용자 (User)
- 사용자 회원가입 및 로그인 (JWT 인증)
- 사용자 정보 조회

### 계좌 (Account)
- 사용자 연동 계좌 생성/삭제
- 계좌 번호로 단건 조회
- 본인 계좌 전체 목록 조회
- 사용자 간 계좌 이체 기능 (트랜잭션 처리 포함)

### 은행 (Bank)
- 은행 코드 및 이름 정보 등록
- 전체 은행 조회, 검색

### 거래내역 (Transaction)
- 출금/입금 거래 내역 기록 (이체 시 자동 생성)
- 계좌별 거래내역 전체 조회
- 거래 ID로 단일 거래 조회

---

## 인증 및 보안

- Spring Security + JWT 기반 인증/인가 구현
- 토큰 기반 Bearer 인증 적용 (Authorization: Bearer <token>)

---

## 기술 스택

- Spring Boot 3.x
- Spring Data JPA (Hibernate)
- Gradle
- H2 Database (개발/테스트용)
- Lombok
- Swagger (Springdoc OpenAPI 3.0)

---

## Swagger API 문서

- 경로: http://localhost:8080/swagger-ui/index.html
- 자동 API 문서화를 통해 테스트 및 협업 편의성 확보

---

## 개발 현황 및 계획

### 현재 구현 완료
- JWT 기반 로그인 인증 시스템
- 사용자 정보 + 계좌 엔티티 설계 및 연결
- 계좌 생성, 삭제, 조회
- 사용자 간 이체 기능 (출금/입금 트랜잭션 자동 기록)
- 거래내역 단건 및 전체 조회 기능
- Swagger 연동

### 예정 기능
- 계좌 비밀번호 검증 로직
- 거래내역 필터링 (기간/타입별)
- 관리자/운영자 역할 기반 권한 제어
- React 기반 프론트엔드 연결
