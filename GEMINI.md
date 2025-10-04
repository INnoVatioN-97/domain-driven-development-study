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

### 🚧 In Progress
- Repository layer (interfaces and implementations)
- Application services (use cases)
- REST API controllers

## Value Object Pattern

This project uses Value Objects extensively for type safety and domain validation:
- `Email`: Email format validation, immutable
- `PhoneNumber`: Phone number normalization ("01012345678" → "010-1234-5678"), masking, carrier detection

Value Objects are embedded using `@Embeddable` and stored in the same table as their parent entity.
