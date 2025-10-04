# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin + Spring Boot study project for learning Domain Driven Development (DDD) methodology. The project implements a ticket reservation system (similar to Interpark) with a focus on proper DDD patterns and hybrid data access strategies.

**Technology Stack:**
- Language: Kotlin 1.9.25
- Framework: Spring Boot 3.5.6
- Java: 21
- Planned: Spring Data JPA (ORM) + MyBatis (RowMapper)
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
./gradlew test --tests "com.innovation.dddexample.domaindrivendevelopmentexample.ClassName.testMethodName"
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
- **Member Aggregate**: Member (Root)

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

Base package: `com.innovation.dddexample.domaindrivendevelopmentexample`

Expected structure (currently being implemented):
- `domain/{context}/model/` - Domain models
- `domain/{context}/repository/` - Repository interfaces
- `domain/{context}/service/` - Domain services
- `application/{context}/` - Application services (use cases)
- `infrastructure/persistence/` - JPA implementations
- `infrastructure/mybatis/` - MyBatis implementations
- `interfaces/rest/{context}/` - REST controllers
- `interfaces/dto/` - Request/Response DTOs

## Development Notes

- This is a **learning project** focused on understanding DDD patterns and Kotlin
- The developer has Java/Spring Boot and Node.js experience but is learning Kotlin
- Emphasis on proper separation of concerns between layers
- Both JPA and MyBatis will be used to compare approaches
