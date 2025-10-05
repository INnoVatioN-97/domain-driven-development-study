# Service Naming Guide

## 개요

이 프로젝트는 DDD와 Clean Architecture 원칙을 따르며, **UseCase**와 **Domain Service**를 명확히 구분하는 네이밍 전략을 사용합니다.

---

## 🎯 네이밍 규칙 요약

| 타입 | 네이밍 | 위치 | 용도 |
|------|--------|------|------|
| **UseCase** | `{Action}{Aggregate}UseCase` | `application/` | 단일 Aggregate 처리 |
| **Domain Service** | `{Aggregate}DomainService` | `domain/{context}/service/` | 여러 Aggregate 처리 |
| **Query Service** | `{Aggregate}QueryService` | `application/` | 조회 전용 (CQRS) |

---

## 1️⃣ UseCase

### 언제 사용?
- ✅ 단일 Aggregate를 중심으로 처리
- ✅ 유스케이스(사용자 행동) 구현
- ✅ 트랜잭션 경계 설정

### 위치
```
application/{context}/
└── {Action}{Aggregate}UseCase.kt
```

### 예시
```kotlin
// application/member/JoinMemberUseCase.kt
@Service
@Transactional
class JoinMemberUseCase(
    private val memberRepository: MemberRepository,
    private val emailService: EmailService
) {
    fun execute(command: JoinMemberCommand): Member {
        // 1. Member Aggregate 생성
        val member = Member(...)

        // 2. 저장
        memberRepository.save(member)

        // 3. 외부 서비스 호출 (이메일 발송)
        emailService.sendWelcome(member)

        return member
    }
}
```

### 실제 UseCase 예시
- `JoinMemberUseCase` - 회원 가입
- `UpdateMemberProfileUseCase` - 회원 프로필 수정
- `ChangeEmailUseCase` - 이메일 변경
- `CreateReservationUseCase` - 예매 생성
- `CancelReservationUseCase` - 예매 취소
- `BookSeatUseCase` - 좌석 예약

---

## 2️⃣ Domain Service

### 언제 사용?
- ✅ 여러 Aggregate를 걸치는 도메인 로직
- ✅ 단일 Aggregate로 해결할 수 없는 도메인 규칙
- ✅ Repository 조회가 필요한 도메인 검증

### 위치
```
domain/{context}/service/
└── {Aggregate}DomainService.kt
```

### 예시
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
     * Repository 조회가 필요한 도메인 규칙
     */
    fun validateUniqueEmail(email: Email) {
        if (memberRepository.existsByEmail(email)) {
            throw DuplicateEmailException(email)
        }
    }
}
```

### 실제 Domain Service 예시
- `MemberDomainService` - 회원 관련 도메인 로직
- `ReservationDomainService` - 예매 관련 도메인 로직
- `SeatAllocationDomainService` - 좌석 배정 로직
- `PaymentDomainService` - 결제 관련 도메인 로직

---

## 3️⃣ Query Service

### 언제 사용?
- ✅ 조회만 수행 (CQRS의 Query)
- ✅ 읽기 전용 트랜잭션
- ✅ 복잡한 조회는 MyBatis 활용 가능

### 위치
```
application/{context}/
└── {Aggregate}QueryService.kt
```

### 예시
```kotlin
// application/member/MemberQueryService.kt
@Service
@Transactional(readOnly = true)  // 읽기 전용 최적화
class MemberQueryService(
    private val memberRepository: MemberRepository
) {
    fun getMemberById(id: Long): Member {
        return memberRepository.findById(id)
            ?: throw MemberNotFoundException(id)
    }

    fun getMemberByEmail(email: Email): Member {
        return memberRepository.findByEmail(email)
            ?: throw MemberNotFoundException(email)
    }
}
```

### 실제 Query Service 예시
- `MemberQueryService` - 회원 조회
- `ReservationQueryService` - 예매 조회
- `PerformanceQueryService` - 공연 조회
- `SeatQueryService` - 좌석 조회

---

## 🔄 UseCase와 Domain Service 협업

### 패턴: UseCase가 Domain Service를 호출

```kotlin
// application/member/WithdrawMemberUseCase.kt
@Service
@Transactional
class WithdrawMemberUseCase(
    private val memberRepository: MemberRepository,
    private val memberDomainService: MemberDomainService  // Domain Service 주입
) {
    fun execute(memberId: Long) {
        // 1. Member 조회
        val member = memberRepository.findById(memberId)
            ?: throw MemberNotFoundException(memberId)

        // 2. Domain Service로 검증 위임 (여러 Aggregate 검증)
        if (!memberDomainService.canWithdraw(member)) {
            throw CannotWithdrawException("진행중인 예매가 있습니다")
        }

        // 3. Member Aggregate의 도메인 로직
        member.withdraw()

        // 4. 저장
        memberRepository.save(member)
    }
}
```

---

## 📂 전체 구조 예시

```
src/main/kotlin/com/innovation/dddexample/
├── domain/
│   ├── member/
│   │   ├── model/
│   │   │   ├── Member.kt                    # Aggregate Root
│   │   │   ├── Email.kt                     # Value Object
│   │   │   └── PhoneNumber.kt               # Value Object
│   │   ├── repository/
│   │   │   └── MemberRepository.kt          # Repository 인터페이스
│   │   ├── exception/
│   │   │   └── MemberNotFoundException.kt   # Domain Exception
│   │   └── service/
│   │       └── MemberDomainService.kt       # Domain Service ⭐
│   │
│   └── reservation/
│       ├── model/
│       │   └── Reservation.kt
│       └── service/
│           └── ReservationDomainService.kt
│
├── application/
│   └── member/
│       ├── MemberQueryService.kt            # Query Service ⭐
│       ├── JoinMemberUseCase.kt             # UseCase ⭐
│       ├── UpdateMemberUseCase.kt           # UseCase ⭐
│       └── WithdrawMemberUseCase.kt         # UseCase ⭐
│
├── infrastructure/
│   └── persistence/member/
│       ├── MemberJpaRepository.kt
│       └── MemberRepositoryImpl.kt
│
└── interfaces/rest/member/
    ├── MemberController.kt
    └── dto/
        └── MemberResponse.kt
```

---

## ⚖️ 결정 트리

### "어느 것을 만들어야 하나?"

```
유스케이스 구현이 필요한가?
│
├─ Yes → 단일 Aggregate만 사용하는가?
│         │
│         ├─ Yes → UseCase 생성
│         │        예: JoinMemberUseCase
│         │
│         └─ No → UseCase 생성 + Domain Service 호출
│                  예: WithdrawMemberUseCase
│                      → MemberDomainService 사용
│
└─ No → 조회만 필요한가?
          │
          ├─ Yes → Query Service 사용
          │        예: MemberQueryService
          │
          └─ No → 여러 Aggregate 걸친 도메인 로직인가?
                   │
                   └─ Yes → Domain Service 생성
                            예: MemberDomainService
```

---

## 🎓 실전 예시

### Case 1: 회원 가입 (단순)
```kotlin
// UseCase만 필요
JoinMemberUseCase
├─ Member 생성 (단일 Aggregate)
├─ Repository 저장
└─ 이메일 발송
```

### Case 2: 회원 가입 (이메일 중복 검증)
```kotlin
// UseCase + Domain Service
JoinMemberUseCase
├─ MemberDomainService.validateUniqueEmail() ← Domain Service 호출
├─ Member 생성
├─ Repository 저장
└─ 이메일 발송

MemberDomainService
└─ validateUniqueEmail()
    └─ Repository 조회로 중복 확인
```

### Case 3: 회원 탈퇴 (예매 확인 필요)
```kotlin
// UseCase + Domain Service
WithdrawMemberUseCase
├─ Member 조회
├─ MemberDomainService.canWithdraw() ← Domain Service 호출
│   └─ Member + Reservation 두 Aggregate 확인
├─ member.withdraw() ← Aggregate 도메인 로직
└─ Repository 저장
```

---

## ❌ 안티패턴

### 피해야 할 것

```kotlin
// ❌ 나쁜 예: 거대한 Service
@Service
class MemberService {
    fun join(...) { }
    fun update(...) { }
    fun withdraw(...) { }
    fun login(...) { }
    fun changePassword(...) { }
    // ... 계속 늘어남
}

// ✅ 좋은 예: 기능별 분리
JoinMemberUseCase
UpdateMemberUseCase
WithdrawMemberUseCase
LoginUseCase
ChangePasswordUseCase
```

```kotlin
// ❌ 나쁜 예: UseCase에 여러 Aggregate 로직
@Service
class WithdrawMemberUseCase {
    fun execute(memberId: Long) {
        val member = ...
        val reservations = reservationRepository.findByMemberId(...)  // ❌
        if (reservations.isNotEmpty()) { ... }  // ❌ 여러 Aggregate 로직
        member.withdraw()
    }
}

// ✅ 좋은 예: Domain Service로 분리
@Service
class WithdrawMemberUseCase(
    private val memberDomainService: MemberDomainService
) {
    fun execute(memberId: Long) {
        val member = ...
        if (!memberDomainService.canWithdraw(member)) { ... }  // ✅
        member.withdraw()
    }
}
```

---

## 📋 체크리스트

새로운 기능을 구현할 때:

- [ ] 단일 Aggregate만 사용? → **UseCase**
- [ ] 여러 Aggregate 걸침? → **Domain Service**
- [ ] 조회만 수행? → **Query Service**
- [ ] UseCase에서 Domain Service 호출? → **OK**
- [ ] Domain Service에서 UseCase 호출? → **NO** (역방향 의존성)

---

## 🔗 참고 자료

- [CLAUDE.md](../CLAUDE.md) - 프로젝트 전체 가이드
- [AGENTS.md](../AGENTS.md) - Service Naming Strategy 섹션
- [kbo-ticket-functional-spec.md](./kbo-ticket-functional-spec.md) - 기능 명세

---

**Updated**: 2025-10-05
**Version**: 1.0
