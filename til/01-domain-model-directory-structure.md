# domain/member/model 디렉토리 구조와 역할

> 작성일: 2025-10-04

## 질문
`domain/member/model/` 디렉토리에는 어떤 파일들이 들어가야 하는가?

## 답변: Aggregate를 구성하는 모든 도메인 모델

`domain/member/model/` 디렉토리에는 **Member Aggregate에 속하는 모든 도메인 모델**이 들어간다.

---

## 들어갈 수 있는 요소들

### 1. Aggregate Root ⭐
- **파일**: `Member.kt`
- **역할**: Aggregate의 진입점이자 루트 엔티티
- **특징**:
  - 고유 식별자(ID) 보유
  - Aggregate 내부의 일관성 보장
  - 외부에서는 반드시 Aggregate Root를 통해서만 접근

### 2. Value Objects (값 객체)
DDD의 핵심 패턴 중 하나

**예시 파일들:**
```
Email.kt              // 이메일 (형식 검증 포함)
PhoneNumber.kt        // 전화번호 (형식 검증 포함)
MemberProfile.kt      // 이름 + 전화번호를 묶은 값 객체
```

**Value Object의 특징:**
- ❌ 식별자(ID)가 없음
- ✅ 불변 객체 (immutable)
- ✅ 값이 같으면 동일한 객체로 취급
- ✅ 자체 검증 로직 포함

**Value Object 예시 코드:**
```kotlin
// domain/member/model/Email.kt
@Embeddable
data class Email(
    @Column(name = "email")
    private val value: String
) {
    init {
        require(value.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) {
            "올바른 이메일 형식이 아닙니다"
        }
    }

    override fun toString(): String = value
}
```

### 3. Entities (엔티티 - Aggregate 내부)
Member Aggregate에 속하면서도 자체 ID를 가진 객체

**예시 파일들:**
```
MembershipGrade.kt    // 회원 등급 (일반/VIP/VVIP 등)
MemberPreference.kt   // 회원 선호 설정 (알림 설정, 관심 카테고리 등)
```

**Entity vs Value Object 비교:**

| 구분 | Entity | Value Object |
|------|--------|--------------|
| 식별자 | ✅ ID 있음 | ❌ ID 없음 |
| 중요한 것 | 연속성 (생명주기) | 값 자체 |
| 가변성 | 상태 변경 가능 | 불변 객체 |
| 예시 | Member, MembershipGrade | Email, PhoneNumber |

### 4. Enums (열거형)
도메인 상태나 타입을 표현

```kotlin
MemberStatus.kt       // ACTIVE, WITHDRAWN, SUSPENDED
MemberType.kt         // INDIVIDUAL, CORPORATE
GradeLevel.kt         // BRONZE, SILVER, GOLD, PLATINUM
```

### 5. 도메인 Exception (선택사항)
도메인 규칙 위반 시 발생하는 예외

```
exceptions/
├── InvalidEmailException.kt
├── MemberAlreadyWithdrawnException.kt
└── DuplicateEmailException.kt
```

---

## 티켓 예매 시스템 기준 실제 구조 예시

```
domain/member/model/
├── Member.kt                           # Aggregate Root ⭐
├── Email.kt                            # Value Object
├── PhoneNumber.kt                      # Value Object
├── MemberProfile.kt                    # Value Object (name + phone 묶음)
├── MemberStatus.kt                     # Enum
├── MembershipGrade.kt                  # Entity (회원 등급)
├── MembershipHistory.kt                # Entity (등급 변경 이력)
└── exceptions/                         # 도메인 예외 (선택)
    ├── DuplicateEmailException.kt
    └── WithdrawnMemberException.kt
```

---

## MVC vs DDD 구조 비교

### MVC 구조
```
model/
├── Member.java          # 단순 DB 테이블 매핑
├── MemberDto.java       # 데이터 전달용
└── MemberVO.java        # 화면 표시용
```

**특징:**
- 모든 것이 평평하게 나열됨
- **기술적 구분** 중심 (DTO, Entity, VO)
- DB 테이블 중심 설계

### DDD 구조
```
domain/member/model/
├── Member.kt            # Aggregate Root
├── Email.kt             # Value Object (도메인 개념)
├── PhoneNumber.kt       # Value Object (도메인 개념)
└── MemberStatus.kt      # Enum (도메인 개념)
```

**특징:**
- **비즈니스 개념 중심** 구조화
- Value Object로 검증 로직 분산
- Aggregate 내부의 응집도 극대화
- 도메인 언어(Ubiquitous Language)로 폴더/파일명 구성

---

## 핵심 정리

### ✅ model/ 디렉토리에 들어가는 것
- Aggregate Root (Member)
- Value Objects (Email, PhoneNumber 등)
- Aggregate 내부 Entities
- Enums
- 도메인 Exceptions

### ❌ model/ 디렉토리에 들어가지 않는 것
- DTO (interfaces/dto 에 위치)
- Repository 인터페이스 (domain/member/repository 에 위치)
- Service (domain/member/service 또는 application/member 에 위치)
- Controller (interfaces/rest/member 에 위치)

---

## Value Object를 사용하는 이유

**현재 Member.kt:**
```kotlin
@Column(nullable = false, unique = true, length = 100)
var email: String
```

**Value Object 적용 후:**
```kotlin
@Embedded
var email: Email
```

**장점:**
1. **검증 로직 캡슐화**: 이메일 형식 검증이 Email 클래스 안에 응집됨
2. **재사용성**: 다른 Aggregate에서도 Email 타입 사용 가능
3. **타입 안전성**: `String`보다 `Email` 타입이 의도를 명확히 표현
4. **불변성**: 한번 생성되면 값 변경 불가 → 안전성 향상

---

## 다음 학습 주제
- [ ] Value Object 실제 적용해보기 (Email, PhoneNumber)
- [ ] Repository 인터페이스 작성
- [ ] Application Service와 Domain Service의 차이점
