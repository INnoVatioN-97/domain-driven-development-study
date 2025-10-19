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
1. **Game Management** - 경기, 팀, 좌석, 등급 정보 관리 (기존 Performance → Game으로 변경)
2. **Reservation** - 예매, 결제, 좌석 점유 처리 (동시성 제어 중요)
3. **Member** - 회원 정보, 예매 이력 관리

**Core Aggregates:**
- **Game Aggregate**: Game (Root), Team (Entity), Seat (Entity), SeatGrade (Entity)
  - Team은 원래 별도 Aggregate였으나 Game 도메인 내부로 이동됨
  - Game은 홈팀/어웨이팀/승자/패자로 Team 참조
- **Reservation Aggregate**: Reservation (Root) - 기본 구조만 구현됨, ReservationItem, Payment는 추후 확장 예정
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

1. **Optimistic Lock**: JPA `@Version` on Seat entity for conflict detection (TODO: 미구현)
2. **Pessimistic Lock**: Applied where necessary with deadlock prevention (TODO: 미구현)
3. Retry logic for handling concurrent booking conflicts (TODO: 미구현)

**Current Status:**
- Seat entity has basic status management (AVAILABLE, SELECTED, RESERVED)
- Business logic for state transitions implemented (reserve(), select(), makeAvailable())
- Concurrency control mechanisms not yet implemented

## Data Access Strategy

This project uses a **Hybrid approach** combining JPA and MyBatis:

- **JPA (Spring Data JPA)**:
  - Domain model persistence (CRUD operations)
  - Entity relationship management
  - Used for: Member, Game, Team, Seat, SeatGrade, Reservation entities
  - Repository pattern: Domain interface → Infrastructure implementation

- **MyBatis**:
  - Complex queries requiring joins and custom SQL
  - Read-optimized queries (CQRS Query side)
  - Used for: Game weekly listing, statistics, reporting
  - Example: `GameMapper.findByDateRange()` - 주간 경기 일정 조회 with Team joins

**When to use which:**
- **JPA**: Single aggregate CRUD, entity lifecycle management, domain logic persistence
- **MyBatis**: Multi-aggregate queries, reporting, complex joins, performance-critical reads

The goal is to understand when to use each approach and how to combine them effectively.

## Package Structure

Base package: `com.innovation.dddexample`

Current structure:
```
com.innovation.dddexample/
├── DddExampleApplication.kt           # Main application class
├── domain/
│   ├── common/                        # ✅ Implemented
│   │   └── exception/                # Common domain exceptions
│   ├── member/                        # ✅ Implemented
│   │   ├── model/                    # Member Aggregate
│   │   │   ├── Member.kt            # Aggregate Root
│   │   │   ├── Email.kt             # Value Object
│   │   │   ├── PhoneNumber.kt       # Value Object
│   │   │   └── Role.kt              # Enum
│   │   ├── repository/              # Repository interface
│   │   ├── service/                 # MemberDomainService
│   │   └── exception/               # Domain-specific exceptions
│   ├── game/                          # ✅ Implemented
│   │   ├── model/                    # Game Aggregate
│   │   │   ├── Game.kt              # Aggregate Root
│   │   │   ├── Team.kt              # Entity (moved to game domain)
│   │   │   ├── Seat.kt              # Entity
│   │   │   ├── SeatGrade.kt         # Entity
│   │   │   ├── GameType.kt          # Enum
│   │   │   └── SeatStatus.kt        # Enum
│   │   └── repository/              # Repository interfaces
│   └── reservation/                   # 🚧 Basic structure
│       ├── model/                    # Reservation Aggregate
│       │   ├── Reservation.kt       # Aggregate Root (basic)
│       │   └── ReservationStatus.kt # Enum
│       └── repository/              # Repository interface
├── application/                       # ✅ Implemented
│   ├── common/                       # UseCase interface
│   ├── member/                       # Member use cases
│   │   ├── SignUpMemberUseCase.kt
│   │   ├── SignInMemberUseCase.kt
│   │   ├── MemberQueryService.kt
│   │   └── Commands (SignUp, SignIn)
│   └── game/                         # Game use cases
│       ├── ListWeeklyGamesUseCase.kt
│       ├── ListAvailableSeatListUseCase.kt
│       └── Commands
├── infrastructure/                    # ✅ Implemented
│   ├── persistence/                  # JPA implementations
│   │   ├── member/                  # MemberRepositoryImpl + JpaRepository
│   │   ├── game/                    # Game, Team, Seat, SeatGrade repositories
│   │   └── reservation/             # ReservationRepositoryImpl + JpaRepository
│   ├── mybatis/                      # MyBatis mappers
│   │   └── game/GameMapper.kt       # Complex game queries
│   ├── security/                     # Security infrastructure
│   │   ├── jwt/                     # JWT token provider
│   │   ├── auth/                    # Authentication services
│   │   └── filter/                  # JWT authentication filter
│   └── seed/                         # Data seeders for development
└── interfaces/                        # ✅ Implemented
    ├── rest/                         # REST controllers
    │   ├── common/                  # Global exception handler
    │   ├── member/                  # MemberController
    │   ├── auth/                    # AuthController
    │   └── game/                    # GameController
    └── dto/                          # Request/Response DTOs
        ├── common/                  # ErrorResponse
        ├── member/                  # MemberResponse
        ├── auth/                    # TokenResponse
        └── game/                    # Game DTOs
```

**Naming Convention:**
- `domain/{context}/model/` - Aggregate Root, Entities, Value Objects
- `domain/{context}/repository/` - Repository interfaces (domain layer)
- `domain/{context}/service/` - Domain services (여러 Aggregate 걸친 도메인 로직)
- `application/{context}/` - Use cases (단일 Aggregate 사용)
- `infrastructure/persistence/{context}/` - JPA repository implementations
- `infrastructure/mybatis/{context}/` - MyBatis mapper implementations
- `interfaces/rest/{context}/` - REST API controllers
- `interfaces/dto/{context}/` - Request/Response DTOs

**Service Naming Strategy:**
- **UseCase** (`~UseCase.kt`): 단일 Aggregate를 다루는 Application 계층 서비스
  - 예: `JoinMemberUseCase`, `UpdateMemberUseCase`, `CreateReservationUseCase`
  - 위치: `application/{context}/`
  - 역할: 유스케이스 조율, 트랜잭션 관리, 인프라 서비스 호출

- **Domain Service** (`~DomainService.kt`): 여러 Aggregate를 걸치는 도메인 로직
  - 예: `MemberDomainService`, `ReservationDomainService`
  - 위치: `domain/{context}/service/`
  - 역할: 단일 Aggregate로 해결할 수 없는 도메인 규칙 처리

- **Query Service** (`~QueryService.kt`): 조회 전용 서비스 (CQRS의 Query)
  - 예: `MemberQueryService` (기존 유지)
  - 위치: `application/{context}/`
  - 역할: 읽기 전용 조회, 복잡한 조회는 MyBatis 활용 가능

## Development Notes

- This is a **learning project** focused on understanding DDD patterns and Kotlin
- The developer has Java/Spring Boot and Node.js experience but is learning Kotlin
- Emphasis on proper separation of concerns between layers
- Both JPA and MyBatis will be used to compare approaches

## Implementation Status

### ✅ Completed

**Domain Layer:**
- **Common Domain**: Base exception classes (DomainException, NotFoundException, DuplicateException, BusinessRuleViolationException)
- **Member Aggregate** (완전 구현):
  - Member entity (Aggregate Root) - Rich domain model with business logic
  - Email Value Object (validation, formatting)
  - PhoneNumber Value Object (validation, normalization, masking)
  - MemberRepository interface
  - MemberDomainService (email uniqueness validation)
  - Domain-specific exceptions (MemberNotFoundException, DuplicateEmailException, InvalidPasswordException)
- **Game Aggregate** (완전 구현):
  - Game entity (Aggregate Root) - 경기 정보, 홈/어웨이 팀 관리
  - Team entity (Game 도메인 내 엔티티) - 팀 정보, 경기장 관리
  - Seat entity - 좌석 상태 관리, 예약/선택 비즈니스 로직
  - SeatGrade entity - 좌석 등급 및 가격 정보
  - Repository interfaces (GameRepository, TeamRepository, SeatRepository, SeatGradeRepository)
  - Enums: GameType, SeatStatus
- **Reservation Aggregate** (기본 구조):
  - Reservation entity (Aggregate Root) - 기본 예매 정보만 포함
  - ReservationStatus enum
  - ReservationRepository interface

**Application Layer:**
- **Member Use Cases**:
  - SignUpMemberUseCase - 회원 가입 + JWT 토큰 발급
  - SignInMemberUseCase - 로그인 인증 + JWT 토큰 발급
  - MemberQueryService - 회원 조회 (CQRS Query)
  - Commands: SignUpMemberCommand, SignInMemberCommand
- **Game Use Cases**:
  - ListWeeklyGamesUseCase - 주간 경기 일정 조회 (MyBatis 활용)
  - ListAvailableSeatListUseCase - 경기별 좌석 조회
  - Commands: ListWeeklyGamesCommand, ListAvailableSeatListCommand
- UseCase interface 정의 (공통 인터페이스)

**Infrastructure Layer:**
- **Persistence (JPA)**:
  - MemberRepositoryImpl + MemberJpaRepository
  - GameRepositoryImpl + GameJpaRepository
  - TeamRepositoryImpl + TeamJpaRepository
  - SeatRepositoryImpl + SeatJpaRepository
  - SeatGradeRepositoryImpl + SeatGradeJpaRepository
  - ReservationRepositoryImpl + ReservationJpaRepository
- **MyBatis Mappers**:
  - GameMapper - 복잡한 게임 조회 쿼리 (주간 일정 조회 등)
  - XML Mapper: `src/main/resources/mapper/game/game.xml`
- **Security**:
  - JWT 인증 시스템 (JwtTokenProvider, JwtProperties)
  - Spring Security 설정 (SecurityConfig)
  - Custom authentication filter (JwtAuthenticationFilter)
  - UserDetailsService 구현 (MemberDetailsService, MemberDetails)
  - SecurityPrincipalResolver - 현재 인증된 사용자 정보 추출
- **Data Seeders**:
  - TeamSeeder, GameSeeder, SeatSeeder
  - DataSeeder - 개발용 초기 데이터 생성

**Interface Layer:**
- **REST Controllers**:
  - MemberController - 회원 조회 API
  - AuthController - 인증 API (회원가입, 로그인)
  - GameController - 경기 및 좌석 조회 API
  - GlobalExceptionHandler - 전역 예외 처리
  - MemberExceptionHandler - Member 도메인 예외 처리
- **DTOs**:
  - Common: ErrorResponse
  - Member: MemberResponse (with extension function)
  - Auth: TokenResponse
  - Game: ListWeeklyGamesResponse, ListAvailableSeatListResponse

**Testing:**
- **Domain Tests**: Member entity, Value Objects (Email, PhoneNumber), exceptions
- **Service Tests**: MemberQueryService, Use Case tests
- **Integration Tests**:
  - MemberControllerIntegrationTest - 14 tests (회원 조회, 전화번호 마스킹, 탈퇴 상태 등)
  - AuthControllerIntegrationTest - 17 tests (회원가입, 로그인, Value Object 검증, 탈퇴 회원 로그인 차단 등)
  - GameControllerIntegrationTest - 10 tests (주간 경기 조회, 좌석 조회, 파라미터 검증 등)
- **Test Coverage**: 43+ integration tests covering happy paths, error cases, edge cases

### 🚧 In Progress / TODO

**Reservation Domain:**
- Reservation aggregate 확장 필요
  - ReservationItem entity 추가 (좌석별 예매 항목)
  - Payment entity 추가 (결제 정보)
  - 예매 생성, 취소, 상태 변경 비즈니스 로직
  - 동시성 제어 (Optimistic/Pessimistic Lock)

**Concurrency Control:**
- Seat entity에 `@Version` 추가 (Optimistic Lock)
- 좌석 예약 시 동시성 처리 로직
- Retry mechanism for concurrent conflicts

**Additional Features:**
- 예매 이력 조회 (MyBatis 활용)
- 통계 및 리포트 기능
- 결제 처리 로직
- 회원 탈퇴 처리 (soft delete 구현됨, UseCase 미구현)

**Known Issues:**
- ⚠️ **GameController GET /games/{gameId}**: 3 tests disabled due to 500 error
  - Requires investigation of:
    1. JOIN FETCH in findGameDetailsById
    2. QueryDSL query execution in findSeatSummaryByGameId
    3. Transaction boundaries and session management
  - See: `GameControllerIntegrationTest.kt` lines 412-466

## Value Object Pattern

This project uses Value Objects extensively for type safety and domain validation:

### Email Value Object
- Email format validation using regex
- Immutable - cannot be changed after creation
- Throws `IllegalArgumentException` if invalid (caught by GlobalExceptionHandler → 400 Bad Request)
- Embedded using `@Embeddable`

### PhoneNumber Value Object
- Phone number normalization: "01012345678" → "010-1234-5678"
- Masking for privacy: "010-1234-5678" → "010-****-5678"
- Carrier detection (SKT, KT, LG U+)
- Validation: Must be 11 digits starting with 010
- Throws `IllegalArgumentException` if invalid

### Domain Entity Validation
- **Member entity**:
  - Name validation in `init` block: `require(name.isNotBlank()) { "회원 이름은 필수입니다" }`
  - Ensures invariants are enforced at construction time
  - Validation also in `updateProfile()` method
- **Validation Strategy**: Fail-fast at entity creation, not just at persistence

Value Objects are embedded using `@Embeddable` and stored in the same table as their parent entity.

## Functional Specifications

Detailed functional specifications are available in:
- [`docs/kbo-ticket-functional-spec.md`](./docs/kbo-ticket-functional-spec.md)

## Testing Guidelines

### Unit Tests
- Use JUnit 5 framework for all tests
- Use `@DisplayName` to describe the test's purpose in Korean
- Test domain logic independently of infrastructure
- Use test fixtures for complex object creation
- Mock external dependencies appropriately

### Integration Tests
- **Controller Integration Tests**: Full Spring context tests for REST API endpoints
  - Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
  - Use `@AutoConfigureMockMvc(addFilters = false)` to disable security filters for testing
  - Use `@Transactional` for automatic rollback after each test
  - Test with real database (MySQL)
  - Example: `MemberControllerIntegrationTest`, `AuthControllerIntegrationTest`, `GameControllerIntegrationTest`

**Integration Test Structure:**
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("Controller 통합 테스트")
class ControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    // Test methods using mockMvc.get/post/put/delete
}
```

**Current Test Coverage:**
- ✅ **MemberControllerIntegrationTest**: 14 tests - Member retrieval, phone masking, withdrawn status
- ✅ **AuthControllerIntegrationTest**: 17 tests - Sign up, sign in, validation, withdrawn member handling
- ✅ **GameControllerIntegrationTest**: 10 tests - Weekly games, seat listing, validation
- ⚠️ **Game detail tests**: 3 tests temporarily disabled due to 500 error (requires debugging)

## Code Style Preferences

- Prefer Kotlin idioms over Java patterns
- Use data classes for DTOs and simple value holders
- Leverage Kotlin's null safety features
- Use extension functions where appropriate
- Keep functions small and focused on single responsibility

## Key DDD Patterns Implemented

### 1. Rich Domain Model vs Anemic Model
- **Member.kt**: Comprehensive example comparing DDD vs MVC approach
- Domain entities contain business logic, not just data
- Methods: `updateProfile()`, `withdraw()`, `changeEmail()`, `isWithdrawn()`, `isActive()`
- Validation and invariants enforced within the entity

### 2. Value Objects
- **Email**: Immutable, self-validating email address
- **PhoneNumber**: Normalization, formatting, masking capabilities
- Embedded using `@Embeddable` in JPA
- Type safety: `Email` type guarantees valid email, not just `String`

### 3. Aggregate Patterns
- **Member Aggregate**: Member (Root) + Email + PhoneNumber
- **Game Aggregate**: Game (Root) + Team + Seat + SeatGrade
  - Seat has business logic: `reserve()`, `select()`, `makeAvailable()`
  - SeatStatus enum: AVAILABLE, SELECTED, RESERVED
- **Reservation Aggregate**: Basic structure (to be expanded)
- Cross-aggregate references use IDs only (e.g., Reservation → memberId, gameId)

### 4. Repository Pattern
- Interfaces in domain layer (`domain/{context}/repository/`)
- Implementations in infrastructure layer (`infrastructure/persistence/{context}/`)
- Separation of domain concerns from persistence technology
- Example: `MemberRepository` interface → `MemberRepositoryImpl` + `MemberJpaRepository`

### 5. Domain Services
- **MemberDomainService**: Email uniqueness validation across aggregates
- Used when business logic spans multiple aggregates
- Located in `domain/{context}/service/`

### 6. Application Services (Use Cases)
- Orchestrate domain objects, don't contain business logic
- Transaction boundaries
- Examples: `SignUpMemberUseCase`, `SignInMemberUseCase`, `ListWeeklyGamesUseCase`
- Command pattern: Each use case has a corresponding Command object

### 7. CQRS (Command Query Responsibility Segregation)
- **Commands**: SignUpMemberUseCase, SignInMemberUseCase (write operations)
- **Queries**: MemberQueryService, ListWeeklyGamesUseCase (read operations)
- Different models for reads vs writes
- MyBatis for complex read queries, JPA for domain persistence

### 8. Domain Exceptions & Exception Handling Strategy
**Exception Hierarchy:**
- `DomainException` (base)
  - `NotFoundException` → HTTP 404
  - `DuplicateException` → HTTP 409
  - `BusinessRuleViolationException` → HTTP 400

**Domain-Specific Exceptions:**
- `MemberNotFoundException`, `GameNotFoundException` → HTTP 404
- `DuplicateEmailException`, `DuplicatePhoneNumberException` → HTTP 409
- `InvalidPasswordException`, `WithdrawnMemberException` → HTTP 400
- `IllegalArgumentException` (Value Object validation) → HTTP 400

**Global Exception Handler:**
- `GlobalExceptionHandler` (interfaces/rest/common/)
  - Handles common domain exceptions (NotFoundException, DuplicateException, BusinessRuleViolationException)
  - Handles IllegalArgumentException for Value Object validation failures
  - Handles MethodArgumentTypeMismatchException for parameter type errors
  - Fallback handler for unexpected exceptions (500 Internal Server Error)
- Domain-specific handlers can be added (e.g., `MemberExceptionHandler`)

**Exception Handling Flow:**
```
1. Domain Layer throws domain exception (e.g., WithdrawnMemberException)
2. Exception bubbles up through Application Layer (UseCase)
3. GlobalExceptionHandler catches and converts to ErrorResponse
4. Client receives appropriate HTTP status code and error message
```

### 9. Ubiquitous Language
- Domain methods named after business concepts, not technical operations
- `withdraw()` instead of `delete()` or `setDeleted(true)`
- `isWithdrawn()` instead of checking `deletedAt != null`
- `reserve()`, `select()` on Seat instead of generic `setStatus()`

### 10. Infrastructure Independence
- Domain layer has no dependencies on infrastructure
- JWT, Security, MyBatis all in infrastructure layer
- Domain can be tested without database or framework

## Learning Points & Best Practices

### DDD vs MVC Comparison
See `Member.kt` (lines 9-263) for detailed inline comparison:
- Anemic vs Rich domain models
- Where business logic lives
- Encapsulation and invariant protection
- Value Objects vs primitive types
- Domain events and ubiquitous language

### Security Integration
- JWT authentication fully integrated with DDD
- `SecurityPrincipalResolver` extracts memberId from security context
- Used in use cases to get current authenticated user
- Example: `ListWeeklyGamesUseCase` checks if user has reservations

### Data Seeding for Development
- `DataSeeder` runs on startup (development only)
- Seeds Teams, Games, Seats for testing
- Demonstrates aggregate creation in code

## Recent Changes

- **2025-10-20**: Integration tests completed
  - Added 43+ controller integration tests (Member, Auth, Game)
  - Implemented IllegalArgumentException handler for Value Object validation failures
  - Added WithdrawnMemberException and withdrawn member login prevention
  - Added Member name validation in `init` block
  - Documented 3 Game detail tests requiring debugging (temporarily disabled)
- **2025-10-19**: Team entity moved from separate aggregate to Game domain (refactoring based on PR #5)
- **2025-10-19**: Weekly games API implemented with MyBatis for optimized querying
- **2025-10-19**: Seat availability listing implemented
- **Earlier**: JWT authentication and security layer implemented
- **Earlier**: Member aggregate with Value Objects completed

## Agent Workflow Addendum (2025-10-05)

- 도메인 작업 착수 전, 해당 도메인 경로(예: `src/main/kotlin/com/innovation/dddexample/domain/member/`)에 요구 정리용 Markdown을 생성해 지침서를 유지한다.
- 요구 정리는 Aggregate 구조, 필수 Repository 기능, 기능 명세 연계 포인트, 테스트 체크리스트, 열린 이슈를 최소 항목으로 포함한다.
- 새 지침서는 `docs`와 소스 경로 모두에서 참조 가능하도록 파일명을 명확히 하고, 이후 구현 단계는 해당 지침을 근거로 진행한다.
