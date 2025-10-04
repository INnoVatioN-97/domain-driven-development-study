# Aggregate와 Value Object 개념 정리

> 작성일: 2025-10-04

## Aggregate란?

**Aggregate**: 하나의 단위로 취급되는 연관된 객체들의 묶음

### 핵심 개념

```
Member Aggregate
├── Member (Aggregate Root) ⭐
├── Email (Value Object)
└── PhoneNumber (Value Object)
```

**비유로 이해하기:**
- Aggregate = 자동차 한 대
- Aggregate Root = 운전대 (외부에서 자동차를 조작하는 유일한 진입점)
- Value Objects = 엔진, 바퀴, 문 (독립적으로 존재하지 않음)

### Aggregate의 규칙

#### 1. 외부에서는 Aggregate Root만 접근 가능
```kotlin
// ❌ 잘못된 접근
member.email.value = "new@example.com"  // Email에 직접 접근

// ✅ 올바른 접근
member.changeEmail(Email("new@example.com"))  // Member를 통해 접근
```

#### 2. Aggregate 내부는 하나의 트랜잭션 단위
```kotlin
// Member + Email + PhoneNumber = 하나의 트랜잭션
// 셋 중 하나라도 실패하면 전체 롤백
memberRepository.save(member)
```

#### 3. Aggregate 경계를 넘는 참조는 ID로만
```kotlin
// ❌ 다른 Aggregate 객체를 직접 참조
class Reservation(
    val member: Member  // Member Aggregate 전체를 참조
)

// ✅ ID로만 참조
class Reservation(
    val memberId: Long  // Member의 ID만 참조
)
```

### MVC vs DDD: Aggregate 관점

**MVC 방식:**
```
Member 테이블 {
    id, email, phone_number, name
}

// 모든 필드가 평평하게 나열됨
// 도메인 개념 없이 DB 테이블 중심
```

**DDD 방식:**
```kotlin
// Member Aggregate
Member (Root) {
    id: Long
    email: Email          // Value Object
    phoneNumber: PhoneNumber  // Value Object
    name: String
}

// 비즈니스 개념 중심으로 구조화
// Member = Aggregate Root
// Email, PhoneNumber = Aggregate의 일부 (Value Object)
```

---

## Value Object란?

**Value Object**: 값 그 자체로 의미를 가지는 불변 객체

### Entity vs Value Object

| 구분 | Entity | Value Object |
|------|--------|--------------|
| 식별자 | ✅ ID 있음 | ❌ ID 없음 |
| 중요한 것 | 연속성 (생명주기) | 값 자체 |
| 비교 기준 | ID | 값 |
| 가변성 | 상태 변경 가능 | 불변 |
| 독립 존재 | 가능 | 불가능 (Aggregate에 종속) |
| 예시 | Member, Reservation | Email, PhoneNumber, Money |

### Value Object의 특징

#### 1. 불변 객체 (Immutable)
```kotlin
@Embeddable
data class Email(
    private val value: String  // val (불변)
) {
    // setter 없음
    // 값 변경 불가
}
```

#### 2. 값 기반 동등성
```kotlin
val email1 = Email("test@example.com")
val email2 = Email("test@example.com")

println(email1 == email2)  // true (값이 같으면 동일)

// 반면 Entity는:
val member1 = Member(id = 1, email = email1)
val member2 = Member(id = 2, email = email1)

println(member1 == member2)  // false (ID가 다르면 다른 회원)
```

#### 3. 자체 검증 (Self-Validation)
```kotlin
@Embeddable
data class Email(
    private val value: String
) {
    init {
        // 생성 시점에 검증
        require(value.matches(EMAIL_REGEX)) {
            "올바른 이메일 형식이 아닙니다"
        }
    }
}

// 사용
val email = Email("invalid")  // 예외 발생!
// → 잘못된 Email 객체는 애초에 존재할 수 없음
```

#### 4. Aggregate에 종속 (@Embeddable)
```kotlin
@Entity
class Member(
    @Embedded
    val email: Email  // Member 테이블의 컬럼으로 저장됨
)

// DB 구조:
// members 테이블 {
//     id,
//     email  ← Email Value Object의 value 필드
// }
//
// email 테이블은 생성되지 않음!
```

---

## 실전 예시: Email Value Object

### MVC 방식 (String 사용)
```kotlin
// Controller
fun updateEmail(memberId: Long, email: String) {
    if (!email.matches(EMAIL_REGEX)) {
        throw Exception("이메일 형식 오류")
    }
    memberService.updateEmail(memberId, email)
}

// Service
fun updateEmail(memberId: Long, email: String) {
    if (!email.matches(EMAIL_REGEX)) {  // 중복 검증!
        throw Exception("이메일 형식 오류")
    }
    val member = memberRepository.findById(memberId)
    member.email = email  // 무조건 할당 가능
    memberRepository.save(member)
}
```

**문제점:**
- 검증 로직이 Controller, Service 여러 곳에 중복
- String 타입이라 실수로 다른 값 할당 가능
- 도메인 의미가 코드에 드러나지 않음

### DDD 방식 (Value Object 사용)
```kotlin
// Controller
fun updateEmail(memberId: Long, emailStr: String) {
    val email = Email(emailStr)  // 여기서 검증 한 번만!
    memberService.updateEmail(memberId, email)
}

// Application Service
fun updateEmail(memberId: Long, email: Email) {
    val member = memberRepository.findById(memberId)
    member.changeEmail(email)  // 도메인 메서드 사용
    memberRepository.save(member)
}

// Member (Aggregate Root)
fun changeEmail(newEmail: Email) {
    require(newEmail != this.email) { "동일한 이메일" }
    this.email = newEmail
}
```

**장점:**
- 검증 로직이 Email 클래스 내부에만 존재 (한 곳에서 관리)
- Email 타입 자체가 "유효한 이메일"을 보장
- 도메인 언어로 표현 (changeEmail)
- 책임 분리 (Email 검증 ← Email, 중복 체크 ← Member)

---

## 실전 예시: PhoneNumber Value Object

### 검증 + 정규화 + 도메인 로직
```kotlin
@Embeddable
data class PhoneNumber(
    private val value: String
) {
    init {
        // 1. 정규화: "01012345678" → "010-1234-5678"
        val normalized = normalizePhoneNumber(value)

        // 2. 검증
        require(normalized.matches(PHONE_REGEX)) {
            "올바른 전화번호 형식이 아닙니다"
        }
    }

    // 3. 도메인 로직
    fun toMasked(): String = value.replaceRange(4, 8, "****")

    fun getCarrier(): String {
        return when {
            value.startsWith("011") -> "SKT"
            value.startsWith("016") -> "KT"
            else -> "알 수 없음"
        }
    }
}
```

**활용:**
```kotlin
val phone = PhoneNumber("01012345678")  // 자동으로 "010-1234-5678"로 정규화

println(phone.toMasked())     // "010-****-5678"
println(phone.getCarrier())   // "알 수 없음"
```

---

## Aggregate + Value Object 통합 예시

### 완성된 Member Aggregate

```kotlin
@Entity
class Member(
    @Id
    val id: Long,

    @Embedded
    var email: Email,          // Value Object

    @Embedded
    var phoneNumber: PhoneNumber,  // Value Object

    var name: String
) {
    // Aggregate Root가 내부 상태 변경을 제어
    fun changeEmail(newEmail: Email) {
        require(newEmail != this.email) { "동일한 이메일" }
        this.email = newEmail
    }

    fun updateProfile(name: String, phoneNumber: PhoneNumber) {
        require(name.isNotBlank()) { "이름 필수" }
        this.name = name
        this.phoneNumber = phoneNumber
    }
}
```

### 사용 예시

```kotlin
// 1. Value Object 생성 (검증 자동 수행)
val email = Email("user@example.com")
val phone = PhoneNumber("01012345678")

// 2. Member Aggregate 생성
val member = Member(
    id = 1L,
    email = email,
    phoneNumber = phone,
    name = "홍길동"
)

// 3. 도메인 메서드를 통한 변경
member.changeEmail(Email("new@example.com"))

// 4. Value Object의 도메인 로직 활용
println(member.phoneNumber.toMasked())  // "010-****-5678"
```

---

## 핵심 정리

### Aggregate
- **정의**: 하나의 트랜잭션 단위로 묶이는 객체들의 집합
- **목적**: 일관성 경계 설정, 복잡도 관리
- **규칙**:
  - Aggregate Root를 통해서만 접근
  - 다른 Aggregate는 ID로만 참조
  - 하나의 트랜잭션 = 하나의 Aggregate

### Value Object
- **정의**: 값 자체로 의미를 가지는 불변 객체
- **목적**: 검증 로직 캡슐화, 타입 안전성, 도메인 언어 표현
- **특징**:
  - ID 없음
  - 불변 객체
  - 값 기반 동등성
  - 자체 검증
  - Aggregate에 종속

### 언제 Value Object를 만드는가?

✅ **Value Object로 만들어야 할 것:**
- Email, PhoneNumber (형식 검증 필요)
- Money, Price (금액 계산 로직)
- Address (여러 필드를 묶어 하나의 개념으로)
- DateRange (시작일~종료일 검증)
- Password (암호화, 정책 검증)

❌ **단순 타입으로 두어도 되는 것:**
- name (특별한 규칙 없는 단순 문자열)
- description (자유 텍스트)

**판단 기준**: "특별한 비즈니스 규칙이나 검증이 필요한가?" → Yes면 Value Object

---

## 다음 학습 주제
- [ ] Repository 인터페이스 작성 (도메인 레이어)
- [ ] Repository 구현체 작성 (인프라 레이어)
- [ ] Application Service vs Domain Service 차이점
- [ ] Aggregate 간 참조 전략 (ID 참조)
