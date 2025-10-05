# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin + Spring Boot study project for learning Domain Driven Development (DDD) methodology. The project implements a ticket reservation system (similar to Interpark) with a focus on proper DDD patterns and hybrid data access strategies.

**Technology Stack:**
- Language: Kotlin 1.9.25
- Framework: Spring Boot 3.5.6
- Java: 21
- Database: MySQL (localhost:3306)
- ORM: Spring Data JPA + MyBatis
- Build Tool: Gradle

## Common Commands

### Build
```bash
./gradlew build
```

### Run Application
```bash
./gradlew bootRun
```

### Run Tests
```bash
./gradlew test
```

### Run Single Test
```bash
./gradlew test --tests "com.innovation.dddexample.ClassName.testMethodName"
```

### Clean Build
```bash
./gradlew clean build
```

## Architecture

This project follows **Domain Driven Development (DDD)** with clear layer separation:

**Bounded Contexts:**
1. **Performance Management** - 공연, 좌석, 등급 정보 관리
2. **Reservation** - 예매, 결제, 좌석 점유 처리 (동시성 제어 중요)
3. **Member** - 회원 정보, 예매 이력 관리

**Core Aggregates:**
- **Performance Aggregate**: Performance (Root), Seat, SeatGrade
- **Reservation Aggregate**: Reservation (Root), ReservationItem, Payment
- **Member Aggregate**: Member (Root), Email (Value Object), PhoneNumber (Value Object)

**Layer Structure:**
```
domain/           - Aggregate, Entity, Value Object, Repository interfaces, Domain Services
application/      - Use Case implementations (orchestrates domain logic)
infrastructure/   - Repository implementations (JPA + MyBatis)
interfaces/       - REST API Controllers, DTOs
```

**Key DDD Principles:**
- Each Aggregate has a clear root entity
- Cross-aggregate references use IDs only, not direct object references
- Domain logic resides in domain layer, not application services
- Repository interfaces defined in domain layer, implemented in infrastructure layer

## Concurrency Control Strategy

The reservation system requires careful concurrency handling for seat booking:

1. **Optimistic Lock**: JPA `@Version` on Seat entity for conflict detection
2. **Pessimistic Lock**: Applied where necessary with deadlock prevention
3. Retry logic for handling concurrent booking conflicts

## Data Access Strategy

- **JPA**: Domain model persistence (Performance, Reservation, Member entities)
- **MyBatis**: Complex queries (reservation history, statistics, reporting)

The goal is to understand when to use each approach and how to combine them effectively.

## Package Structure

Base package: `com.innovation.dddexample`

Current structure:
```
com.innovation.dddexample/
├── DddExampleApplication.kt           # Main application class
├── domain/
│   └── member/
│       ├── model/                      # ✅ Implemented
│       │   ├── Member.kt              # Aggregate Root
│       │   ├── Email.kt               # Value Object
│       │   └── PhoneNumber.kt         # Value Object
│       ├── repository/                # TODO
│       └── service/                   # TODO
├── application/                       # TODO
├── infrastructure/                    # TODO
│   ├── persistence/                  # JPA implementations
│   └── mybatis/                      # MyBatis implementations
└── interfaces/                        # TODO
    ├── rest/                         # REST controllers
    └── dto/                          # Request/Response DTOs
```

**Naming Convention:**
- `domain/{context}/model/` - Aggregate Root, Entities, Value Objects
- `domain/{context}/repository/` - Repository interfaces (domain layer)
- `domain/{context}/service/` - Domain services
- `application/{context}/` - Application services (use cases)
- `infrastructure/persistence/{context}/` - JPA repository implementations
- `infrastructure/mybatis/{context}/` - MyBatis mapper implementations
- `interfaces/rest/{context}/` - REST API controllers
- `interfaces/dto/{context}/` - Request/Response DTOs

## Development Notes

- This is a **learning project** focused on understanding DDD patterns and Kotlin
- The developer has Java/Spring Boot and Node.js experience but is learning Kotlin
- Emphasis on proper separation of concerns between layers
- Both JPA and MyBatis will be used to compare approaches

## Implementation Status

### ✅ Completed
- Database configuration (MySQL)
- Member Aggregate with Value Objects
  - Member entity (Aggregate Root)
  - Email Value Object (validation, formatting)
  - PhoneNumber Value Object (validation, normalization, masking)
- Package structure refactored to `com.innovation.dddexample`
- Member Repository layer
  - MemberRepository interface (domain layer)
  - MemberJpaRepository (Spring Data JPA)
  - MemberRepositoryImpl (infrastructure layer)

### 🚧 In Progress
- Member GET API (Reference Implementation)
  - Application service (MemberQueryService)
  - REST controller (MemberController)
  - DTOs and exception handling

## Reference Implementation: Member GET API

**Feature**: GET /api/members/{id} - Retrieve member information by ID

This implementation serves as a **reference pattern** for all future DDD-based APIs in the project.

### Layer-by-Layer Implementation

#### 1. Domain Layer (No changes needed)
- **Member** aggregate already exists with Value Objects
- **MemberRepository** interface defines contract: `fun findById(id: Long): Member?`
- **MemberNotFoundException** domain exception created for not-found scenario

```kotlin
// domain/member/exception/MemberNotFoundException.kt
class MemberNotFoundException(
    memberId: Long
) : RuntimeException("Member not found with id: $memberId")
```

**Key Principle**: Domain layer knows nothing about HTTP, REST, or Spring Web.

#### 2. Infrastructure Layer (Existing)
- **MemberRepositoryImpl** implements domain interface
- Delegates to **MemberJpaRepository** (Spring Data JPA)
- Converts JPA `Optional<Member>` to Kotlin nullable `Member?`

**Key Principle**: Infrastructure knows about JPA but domain doesn't.

#### 3. Application Layer (New)
- **MemberQueryService** orchestrates the use case

```kotlin
// application/member/MemberQueryService.kt
@Service
@Transactional(readOnly = true)
class MemberQueryService(
    private val memberRepository: MemberRepository
) {
    fun getMemberById(id: Long): Member {
        return memberRepository.findById(id)
            ?: throw MemberNotFoundException(id)
    }
}
```

**Key Principles**:
- Thin service: delegates to repository, throws domain exception
- `@Transactional(readOnly = true)` for read optimization
- Returns domain entity (Member), not DTO
- Reusable across different interfaces (REST, GraphQL, CLI)

#### 4. Interface Layer (New)
- **MemberResponse** DTO for API contract
- **MemberController** handles HTTP concerns
- **Mapper** transforms domain entity to DTO

```kotlin
// interfaces/dto/member/MemberResponse.kt
data class MemberResponse(
    val id: Long,
    val name: String,
    val email: String,              // from Email.value
    val phoneNumber: String,        // from PhoneNumber.masked (privacy!)
    val status: String,             // "ACTIVE" or "WITHDRAWN"
    val pointBalance: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// Extension function for mapping
fun Member.toResponse(): MemberResponse = MemberResponse(
    id = this.id!!,
    name = this.name,
    email = this.email.value,
    phoneNumber = this.phoneNumber.masked,  // Privacy via Value Object!
    status = if (isWithdrawn()) "WITHDRAWN" else "ACTIVE",
    pointBalance = 0,
    createdAt = this.registeredAt,
    updatedAt = this.registeredAt
)
```

```kotlin
// interfaces/rest/member/MemberController.kt
@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberQueryService: MemberQueryService
) {
    @GetMapping("/{id}")
    fun getMember(@PathVariable id: Long): MemberResponse {
        val member = memberQueryService.getMemberById(id)
        return member.toResponse()
    }

    @ExceptionHandler(MemberNotFoundException::class)
    fun handleNotFound(ex: MemberNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(message = ex.message))
    }
}
```

**Key Principles**:
- Controller is thin: calls service, maps to DTO, handles exceptions
- DTO uses Value Object properties (email.value, phoneNumber.masked)
- Domain exception → HTTP status translation happens here
- Privacy enforced via PhoneNumber.masked (e.g., "010-****-5678")

### Dependency Flow

```
Controller (interfaces)
    ↓ depends on
Application Service (application)
    ↓ depends on
Repository Interface (domain) ← DEFINED HERE
    ↑ implemented by
Repository Impl (infrastructure)
```

**Key Insight**: Domain layer is at the center. All dependencies point inward.

### Data Flow

```
HTTP Request
    ↓
Controller: Parse ID, call service
    ↓
Service: Call repository, throw if not found
    ↓
Repository Impl: Delegate to JPA, convert Optional
    ↓
JPA: Query database
    ↓
Database: Return row
    ↑
JPA: Map to Member entity
    ↑
Repository Impl: Return Member or null
    ↑
Service: Return Member or throw MemberNotFoundException
    ↑
Controller: Catch exception → 404 OR map to MemberResponse
    ↑
HTTP Response: JSON with masked phone number
```

### Privacy Pattern

**Problem**: Phone numbers are sensitive and should be partially masked in API responses.

**Solution**: Use PhoneNumber Value Object's `masked` property.

```kotlin
// Domain model defines the capability
@Embeddable
class PhoneNumber(value: String) {
    val value: String = normalize(value)
    val formatted: String = format(value)  // "010-1234-5678"
    val masked: String = mask(value)       // "010-****-5678"
}

// DTO leverages it
fun Member.toResponse() = MemberResponse(
    phoneNumber = this.phoneNumber.masked  // Privacy enforced here
)
```

**Key Insight**: Privacy logic lives in the Value Object, not scattered across DTOs or services.

### Exception Handling Pattern

**Problem**: How to handle "not found" scenario while maintaining layering?

**Solution**: Domain exception + controller translation.

```kotlin
// Domain layer: business language
throw MemberNotFoundException(memberId)

// Interface layer: HTTP translation
@ExceptionHandler(MemberNotFoundException::class)
fun handleNotFound(ex: MemberNotFoundException): ResponseEntity<ErrorResponse> {
    return ResponseEntity.status(404).body(ErrorResponse(message = ex.message))
}
```

**Key Insight**: Domain doesn't know about HTTP. Controller translates domain concepts to HTTP.

## Value Object Pattern

This project uses Value Objects extensively for type safety and domain validation:
- `Email`: Email format validation, immutable
- `PhoneNumber`: Phone number normalization ("01012345678" → "010-1234-5678"), masking, carrier detection

Value Objects are embedded using `@Embeddable` and stored in the same table as their parent entity.

## Functional Specifications

Detailed functional specifications are available in:
- [`docs/kbo-ticket-functional-spec.md`](./docs/kbo-ticket-functional-spec.md)

## Testing Guidelines

When writing tests:
- Use Kotest framework for all tests
- Follow BDD style (Given-When-Then)
- Test domain logic independently of infrastructure
- Use test fixtures for complex object creation
- Mock external dependencies appropriately

## Code Style Preferences

- Prefer Kotlin idioms over Java patterns
- Use data classes for DTOs and simple value holders
- Leverage Kotlin's null safety features
- Use extension functions where appropriate
- Keep functions small and focused on single responsibility

## Service Naming Strategy (Updated 2025-10-05)

이 프로젝트는 **UseCase와 Domain Service를 명확히 구분**하는 네이밍 전략을 사용합니다.

### UseCase: 단일 Aggregate 처리

**위치**: `application/{context}/`
**네이밍**: `{Action}{Aggregate}UseCase.kt`
**역할**: 하나의 Aggregate를 중심으로 한 유스케이스 처리

```kotlin
// application/member/JoinMemberUseCase.kt
@Service
@Transactional
class JoinMemberUseCase(
    private val memberRepository: MemberRepository,
    private val emailService: EmailService
) {
    fun execute(command: JoinMemberCommand): Member {
        // 1. Member Aggregate 생성 (도메인 로직)
        val member = Member(
            email = Email(command.email),
            name = command.name,
            phoneNumber = PhoneNumber(command.phoneNumber)
        )

        // 2. 저장
        memberRepository.save(member)

        // 3. 이메일 발송 (인프라)
        emailService.sendWelcomeEmail(member)

        return member
    }
}
```

**예시**:
- `JoinMemberUseCase` - 회원 가입
- `UpdateMemberUseCase` - 회원 정보 수정
- `CreateReservationUseCase` - 예매 생성
- `CancelReservationUseCase` - 예매 취소

### Domain Service: 여러 Aggregate 처리

**위치**: `domain/{context}/service/`
**네이밍**: `{Aggregate}DomainService.kt`
**역할**: 여러 Aggregate를 걸치는 도메인 로직

```kotlin
// domain/member/service/MemberDomainService.kt
class MemberDomainService(
    private val memberRepository: MemberRepository,
    private val reservationRepository: ReservationRepository
) {
    /**
     * 회원 탈퇴 가능 여부 검증
     * Member와 Reservation 두 Aggregate 사용
     */
    fun canWithdraw(member: Member): Boolean {
        val activeReservations = reservationRepository
            .findActiveByMemberId(member.id!!)
        return activeReservations.isEmpty()
    }

    /**
     * 이메일 중복 검증
     * 단일 Aggregate지만 Repository 조회가 필요한 도메인 규칙
     */
    fun validateUniqueEmail(email: Email) {
        if (memberRepository.existsByEmail(email)) {
            throw DuplicateEmailException(email)
        }
    }
}
```

**예시**:
- `MemberDomainService` - 회원 관련 도메인 로직
- `ReservationDomainService` - 예매 관련 도메인 로직
- `SeatAllocationService` - 좌석 배정 로직

### Query Service: 조회 전용 (CQRS)

**위치**: `application/{context}/`
**네이밍**: `{Aggregate}QueryService.kt`
**역할**: 읽기 전용 조회

```kotlin
// application/member/MemberQueryService.kt
@Service
@Transactional(readOnly = true)
class MemberQueryService(
    private val memberRepository: MemberRepository
) {
    fun getMemberById(id: Long): Member {
        return memberRepository.findById(id)
            ?: throw MemberNotFoundException(id)
    }
}
```

**예시**:
- `MemberQueryService` - 회원 조회
- `ReservationQueryService` - 예매 조회
- `PerformanceQueryService` - 공연 조회

### UseCase에서 Domain Service 사용

```kotlin
// application/member/WithdrawMemberUseCase.kt
@Service
@Transactional
class WithdrawMemberUseCase(
    private val memberRepository: MemberRepository,
    private val memberDomainService: MemberDomainService  // Domain Service 주입
) {
    fun execute(memberId: Long) {
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException(memberId)

        // Domain Service로 검증 위임 (여러 Aggregate 검증)
        if (!memberDomainService.canWithdraw(member)) {
            throw CannotWithdrawException("진행중인 예매가 있습니다")
        }

        // Member Aggregate의 도메인 로직
        member.withdraw()
        memberRepository.save(member)
    }
}
```

### 전체 구조 예시

```
src/main/kotlin/com/innovation/dddexample/
├── domain/
│   └── member/
│       ├── model/
│       │   └── Member.kt              # Aggregate Root
│       ├── repository/
│       │   └── MemberRepository.kt    # Repository 인터페이스
│       └── service/
│           └── MemberDomainService.kt # Domain Service (여러 Aggregate)
│
├── application/
│   └── member/
│       ├── MemberQueryService.kt      # Query (조회)
│       ├── JoinMemberUseCase.kt       # UseCase (가입)
│       ├── UpdateMemberUseCase.kt     # UseCase (수정)
│       └── WithdrawMemberUseCase.kt   # UseCase (탈퇴, Domain Service 사용)
│
└── interfaces/rest/member/
    └── MemberController.kt            # REST Controller
```

### 네이밍 결정 기준

| 상황 | 선택 | 예시 |
|------|------|------|
| 단일 Aggregate만 사용 | `~UseCase` | `JoinMemberUseCase` |
| 여러 Aggregate 걸침 | `~DomainService` | `MemberDomainService` |
| 조회만 (CQRS Query) | `~QueryService` | `MemberQueryService` |

## Agent Workflow Addendum (2025-10-05)

- 도메인 작업 착수 전, 해당 도메인 경로(예: `src/main/kotlin/com/innovation/dddexample/domain/member/`)에 요구 정리용 Markdown을 생성해 지침서를 유지한다.
- 요구 정리는 Aggregate 구조, 필수 Repository 기능, 기능 명세 연계 포인트, 테스트 체크리스트, 열린 이슈를 최소 항목으로 포함한다.
- 새 지침서는 `docs`와 소스 경로 모두에서 참조 가능하도록 파일명을 명확히 하고, 이후 구현 단계는 해당 지침을 근거로 진행한다.
