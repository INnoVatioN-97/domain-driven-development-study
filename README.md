# DDD Example Project

Domain Driven Development 학습을 위한 Kotlin + Spring Boot 프로젝트

## 프로젝트 목적

이 프로젝트는 DDD(Domain Driven Development) 방법론과 Kotlin 언어를 학습하기 위한 스터디 프로젝트입니다.

## 학습 목표

### 1. Kotlin 문법 스터디
- Java/Spring Boot 및 Node.js 개발 경험을 기반으로 Kotlin 언어 학습
- Kotlin의 주요 특징 및 관용구(idioms) 이해
- Spring Boot와 Kotlin의 통합 방식 습득

### 2. DDD 방법론 학습
- Domain Driven Development의 핵심 개념 이해
- 실제 웹 서비스를 DDD 패턴으로 설계하고 구현
- Bounded Context, Aggregate, Entity, Value Object 등의 개념 적용
- Domain Layer, Application Layer, Infrastructure Layer 분리

### 3. 하이브리드 데이터 접근 방식
- **ORM**: Spring Data JPA를 활용한 객체 중심 데이터 접근
- **RowMapper**: MyBatis를 활용한 쿼리 중심 데이터 접근
- 각 방식의 장단점 이해 및 적절한 혼용 전략 수립

## 기술 스택

- **Language**: Kotlin
- **Framework**: Spring Boot
- **Data Access**:
  - Spring Data JPA (ORM)
  - MyBatis (RowMapper)
- **Build Tool**: Gradle
- **Architecture**: Domain Driven Development (DDD)

## 도메인: 티켓 예매 시스템

인터파크와 같은 공연 티켓 예매 서비스를 구현합니다.

### Bounded Context

**1. Performance Management (공연 관리)**
- 공연, 좌석, 등급 정보 관리

**2. Reservation (예매)**
- 예매, 결제, 좌석 점유 처리
- **동시성 제어**: 여러 사용자가 동시에 같은 좌석을 예매하려 할 때 처리

**3. Member (회원)**
- 회원 정보, 예매 이력 관리

### 핵심 Aggregate

**Performance Aggregate**
- Performance (공연 - Aggregate Root)
  - id, title, description, venue, performanceDate
- Seat (좌석)
  - id, seatNumber, seatGrade, status, version (낙관적 락)
- SeatGrade (좌석 등급)
  - grade, price

**Reservation Aggregate**
- Reservation (예매 - Aggregate Root)
  - id, memberId, reservationDate, status, totalAmount
- ReservationItem (예매 항목)
  - seatId, price
- Payment (결제)
  - paymentMethod, paymentDate, amount

**Member Aggregate**
- Member (회원 - Aggregate Root)
  - id, name, email, phoneNumber

### 동시성 제어 전략

1. **낙관적 락 (Optimistic Lock)**
   - JPA @Version을 사용한 좌석 상태 관리
   - 충돌 시 재시도 로직 구현

2. **비관적 락 (Pessimistic Lock)**
   - 필요시 좌석 점유에 적용
   - 데드락 방지 전략 포함

## 프로젝트 구조

```
src/main/kotlin/com/innovation/dddexample/
├── domain/                      # 도메인 레이어
│   ├── performance/             # 공연 도메인
│   │   ├── model/              # Aggregate, Entity, Value Object
│   │   ├── repository/         # Repository Interface
│   │   └── service/            # Domain Service
│   ├── reservation/            # 예매 도메인
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   └── member/                 # 회원 도메인
│       ├── model/
│       ├── repository/
│       └── service/
├── application/                # 애플리케이션 레이어
│   ├── performance/            # Use Case 구현
│   ├── reservation/
│   └── member/
├── infrastructure/             # 인프라 레이어
│   ├── persistence/            # JPA 구현체
│   └── mybatis/                # MyBatis 구현체
└── interfaces/                 # 인터페이스 레이어
    ├── rest/                   # REST API Controller
    │   ├── performance/
    │   ├── reservation/
    │   └── member/
    └── dto/                    # Request/Response DTO
```

### 데이터 접근 전략

- **JPA 사용**: 도메인 모델 영속화 (Performance, Reservation, Member)
- **MyBatis 사용**: 복잡한 조회 쿼리 (예매 내역 조회, 통계 등)

## 시작하기

### 빌드
```bash
./gradlew build
```

### 실행
```bash
./gradlew bootRun
```

## 학습 진행 사항

- [x] 프로젝트 초기 설정
- [x] DDD 도메인 소재 선정 (티켓 예매 시스템)
- [x] 도메인 모델링 (Bounded Context, Aggregate 정의)
- [x] 프로젝트 패키지 구조 생성
- [ ] 도메인 모델 구현 (Entity, Value Object)
- [ ] Repository 인터페이스 정의
- [ ] JPA 구현체 작성
- [ ] MyBatis 구현체 작성
- [ ] Application Service (Use Case) 구현
- [ ] REST API Controller 구현
- [ ] 동시성 제어 로직 구현 및 테스트
- [ ] JPA vs MyBatis 사용 사례 정리
