# 💳 OpenBanking API

Spring Boot 기반 웹 오픈뱅킹 시스템입니다.  
실제 사용자 인증을 기반으로 계좌 개설, 조회, 이체, 거래내역 조회까지 가능한  
RESTful API 서버를 구현하고 있습니다.

---

## 🚀 주요 기능 요약

### 사용자 (User)
- 사용자 회원가입 및 로그인 (JWT 인증)
- 사용자 정보 조회

### 계좌 (Account)
- 사용자 연동 계좌 생성/삭제
- 계좌 번호로 단건 조회
- 본인 계좌 전체 목록 조회
- 사용자 간 계좌 이체 기능 (트랜잭션 처리 포함)

### 알림 (Notification)
- 계좌 개설, 삭제, 이체 시 알림 자동 발송
- 사용자 알림 목록 조회, 특정 알림 읽음 처리 기능 포함
- 예정: 잔액 부족, 고액 거래, 월간 리포트, 예약 이체 성공/실패 알림 확장 예정

### 은행 (Bank)
- 은행 코드 및 이름 정보 등록
- 전체 은행 조회, 검색

### 거래내역 (Transaction)
- 출금/입금 거래 내역 기록 (이체 시 자동 생성)
- 계좌별 거래내역 전체 조회
- 거래 ID로 단일 거래 조회

### 예약이체 (Scheduled Transfer) 
- 예약 이체 등록 (예약 시각 지정)
- 내 예약이체 목록 조회
- 예약이체 성공/실패 시 알림 자동 발송
- 배치 기반 1분 주기 예약이체 자동 실행

---

## 🔐 인증 및 보안

- Spring Security + JWT 기반 인증/인가 구현
- 토큰 기반 Bearer 인증 적용  
  (헤더: `Authorization: Bearer <token>`)

---

## 🛠 기술 스택

- Spring Boot 3.x
- Spring Data JPA (Hibernate)
- Gradle
- H2 Database (개발/테스트용)
- Lombok
- Swagger (Springdoc OpenAPI 3.0)

---

## 📘 Swagger API 문서

### UserController

| Method | Endpoint       | Description            |
|--------|----------------|------------------------|
| GET    | /users         | 전체 사용자 목록 조회    |
| POST   | /users         | 사용자 등록             |
| GET    | /users/{id}    | 사용자 상세 조회         |
| DELETE | /users/{id}    | 사용자 삭제             |

### TransactionController

| Method | Endpoint                                | Description              |
|--------|-----------------------------------------|--------------------------|
| POST   | /transactions                           | 거래 생성                 |
| GET    | /transactions/{transactionId}           | 특정 거래 상세 조회       |
| GET    | /transactions/account/{accountId}       | 계좌 기준 거래내역 조회    |

### BankController

| Method | Endpoint        | Description              |
|--------|-----------------|--------------------------|
| GET    | /banks          | 은행 목록 조회             |
| POST   | /banks          | 은행 등록                 |
| GET    | /banks/{id}     | 특정 은행 정보 조회         |
| DELETE | /banks/{id}     | 은행 삭제                 |
| GET    | /banks/search   | 은행 검색 (이름/코드 등)    |

### ScheduledTransferController

| Method | Endpoint                          | Description                      |
|--------|-----------------------------------|----------------------------------|
| POST   | /api/scheduled-transfers          | 예약이체 등록 (예약 시각 지정)    |
| GET    | /api/scheduled-transfers/my       | 내 예약이체 목록 조회              |

### NotificationController

| Method | Endpoint | Description |
|--------|------------------------------|-----------------------------------|
| POST   | /api/notifications           | 알림 직접 생성 (관리자용) |
| POST   | /api/notifications/read/{id} | 특정 알림 읽음 처리 |
| GET    | /api/notifications/my        | 내 알림 목록 조회 |

### AuthController

| Method | Endpoint     | Description     |
|--------|--------------|-----------------|
| POST   | /auth/login  | 로그인 (JWT 인증) |

### AccountController

| Method | Endpoint                              | Description                      |
|--------|---------------------------------------|----------------------------------|
| POST   | /accounts                             | 계좌 생성                         |
| POST   | /accounts/transfer                    | 계좌 간 이체                      |
| GET    | /accounts/{accountId}                 | 특정 계좌 상세 조회               |
| DELETE | /accounts/{accountId}                 | 계좌 삭제                         |
| GET    | /accounts/number/{accountNumber}      | 계좌번호로 계좌 조회              |
| GET    | /accounts/my                          | 내 계좌 목록 조회 (JWT 인증 필요) |

### HelloController

| Method | Endpoint | Description        |
|--------|----------|---------------------|
| GET    | /        | 서버 기본 응답 확인  |

---
<img width="1233" height="734" alt="image" src="https://github.com/user-attachments/assets/7b1d4029-9bf0-46a2-98c4-cbbc4c4cf236" />


## 📈 개발 현황 및 계획

### 현재 구현 완료
- JWT 기반 로그인 인증 시스템
- 사용자 정보 및 계좌 엔티티 설계 및 연동
- 계좌 생성, 삭제, 단건 및 전체 조회
- 사용자 간 이체 기능 (출금/입금 트랜잭션 자동 기록)
- 거래내역 단건 및 전체 조회
- 예약이체 기능(배치 기반 1분 주기 예약 이체 실행 처리)
- 예약이체 목록 조회 api
- Swagger, React 프론트 연동 완료

### 예정 기능
- 계좌 비밀번호 검증 로직 추가
- 거래내역 필터링 (기간/타입별 조회)
- 관리자/운영자 역할 기반 권한 제어
- 예약 이체 취소 API, 예약 이체 실패시 자동 재시도 처리, kafka 기반 처리 도입
