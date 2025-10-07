# DDD 기반 티켓 예매 시스템

DDD(Domain Driven Development) 방법론 학습을 위한 Kotlin + Spring Boot 프로젝트입니다.

## 프로젝트 목적

이 프로젝트는 DDD 방법론과 Kotlin 언어를 학습하고, 실제 웹 서비스에 적용하는 경험을 쌓기 위한 스터디 프로젝트입니다. 인터파크와 같은 공연 티켓 예매 서비스를 도메인 주도 설계 방식으로 구현합니다.

## 학습 목표

- **Kotlin Idioms**: Java 경험을 바탕으로 코틀린의 관용적인 표현과 주요 특징을 학습합니다.
- **DDD Practice**: Bounded Context, Aggregate, Value Object 등 DDD 핵심 개념을 실제 코드에 적용하고, 계층형 아키텍처를 구축합니다.
- **Hybrid Data Access**: ORM(JPA)과 SQL Mapper(MyBatis)의 장단점을 이해하고, 상황에 맞는 기술을 선택하여 하이브리드 데이터 접근 전략을 수립합니다.
- **Clean Architecture**: 도메인, 애플리케이션, 인프라, 인터페이스 계층을 명확히 분리하여 유지보수성과 확장성이 뛰어난 구조를 설계합니다.

## 기술 스택

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.6
- **Java**: 21
- **Database**: MySQL
- **Data Access**: Spring Data JPA, MyBatis
- **Authentication**: Spring Security, JWT
- **Build Tool**: Gradle
- **Architecture**: Domain Driven Development (DDD)

## 도메인: 티켓 예매 시스템

### Bounded Context

1.  **Member (회원)**: 회원 정보, 예매 이력 관리
2.  **Performance (공연)**: 공연, 좌석, 등급 정보 관리
3.  **Reservation (예매)**: 예매, 결제, 좌석 점유 처리 (동시성 제어)

### 핵심 Aggregate

-   **Member Aggregate**: ✅ **구현 완료 (JWT 인증 포함)**
    -   `Member` (Root), `Email` (VO), `PhoneNumber` (VO)
-   **Performance Aggregate**: 🚧 **구현 중**
    -   `Team` (Entity), `Game` (Entity) - 도메인 모델링 및 시더 구현 완료
-   **Reservation Aggregate**: 🚧 **구현 예정**
    -   `Reservation` (Root), `ReservationItem`, `Payment`

## 프로젝트 구조 (현재)

`member`, `team`, `game` 도메인 모델과 시더가 구현된 현재 프로젝트 구조입니다.

```
src/main/kotlin/com/innovation/dddexample/
├── DddExampleApplication.kt
├── application
│   └── member/
├── domain
│   ├── game/
│   │   ├── model/
│   │   │   ├── Game.kt
│   │   │   └── GameType.kt
│   │   └── repository/
│   │       └── GameRepository.kt
│   ├── member/
│   │   └── ... (생략)
│   └── team/
│       ├── model/
│       │   └── Team.kt
│       └── repository/
│           └── TeamRepository.kt
├── infrastructure
│   ├── persistence/
│   │   ├── member/
│   │   └── team/
│   ├── seed/
│   │   ├── DataSeeder.kt
│   │   ├── game/GameSeeder.kt
│   │   └── team/TeamSeeder.kt
│   └── security/
│       └── ... (생략)
└── interfaces
    └── ... (생략)
```

## 구현 현황

### ✅ `v1.0` - 회원 도메인 및 인증 시스템
- **Domain Layer**: `Member` Aggregate, `Email`/`PhoneNumber` Value Object 구현
- **Application Layer**: `SignUpUseCase`, `SignInUseCase`, `MemberQueryService` 구현
- **Infrastructure Layer**: `MemberRepository`를 JPA로 구현
- **Interface Layer**: 회원가입, 로그인, 정보 조회를 위한 REST API (`AuthController`, `MemberController`) 구현
- **Security**: Spring Security와 JWT를 연동한 인증/인가 시스템 구축
- **Testing**: 주요 로직에 대한 단위/통합 테스트 작성

### 🚧 `v1.5` - 공연 도메인 기반 마련 (진행 중)
- **Domain Layer**: `Team`, `Game` 엔티티 모델링 (DDD 원칙 기반 리팩토링 완료)
- **Infrastructure Layer**: `local`/`dev` 프로파일용 데이터 시더 구현 (KBO 10개 구단 및 720경기 전체 일정 자동 생성)

### 🚧 `v2.0` - 공연 및 예매 도메인 (예정)
- `Performance` Aggregate 구현 (공연, 좌석, 등급 관리)
- `Reservation` Aggregate 구현 (예매, 결제)
- 동시성 제어 (Optimistic/Pessimistic Lock) 적용 및 테스트
- MyBatis를 활용한 복잡한 조회 기능 구현

## 시작하기

### 빌드
```bash
./gradlew build
```

### 실행

`local` 또는 `dev` 프로파일로 실행 시, 애플리케이션이 시작되면서 KBO 10개 구단 및 정규시즌 720경기 데이터가 자동으로 DB에 생성됩니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## 학습 자료

프로젝트를 진행하며 배운 내용은 `til/` 디렉토리에 정리합니다.
- `til/01-domain-model-directory-structure.md`
- `til/02-aggregate-and-value-object.md`