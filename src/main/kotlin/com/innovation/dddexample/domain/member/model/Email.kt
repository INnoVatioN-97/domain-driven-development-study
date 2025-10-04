package com.innovation.dddexample.domain.member.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * Email Value Object
 *
 * [Value Object란?]
 * - DDD의 핵심 패턴 중 하나
 * - "값 그 자체"로 의미를 가지는 불변 객체
 * - 식별자(ID)가 없으며, 값이 같으면 동일한 객체로 취급
 *
 * [MVC vs DDD]
 * MVC: email을 단순 String으로 처리
 *      - 검증 로직이 Controller, Service 등 여러 곳에 흩어짐
 *      - "abc123" 같은 잘못된 값도 할당 가능
 *
 * DDD: Email이라는 독립적인 타입으로 분리
 *      - 검증 로직이 Email 클래스 내부에 캡슐화됨
 *      - 잘못된 Email 객체는 애초에 생성될 수 없음 (생성자에서 검증)
 *      - 타입 안전성: String보다 Email이 의도를 명확히 표현
 *
 * [왜 Value Object를 사용하는가?]
 * 1. 검증 로직 중앙화: 이메일 형식 체크가 한 곳에만 존재
 * 2. 재사용성: 다른 Aggregate에서도 Email 타입 사용 가능
 * 3. 불변성: 한번 생성되면 절대 변경 불가 → 안전성 보장
 * 4. 도메인 언어: "String email"보다 "Email email"이 의미 명확
 *
 * [@Embeddable이란?]
 * - JPA에서 Value Object를 별도 테이블이 아닌 "부모 엔티티의 컬럼"으로 저장
 * - Email 테이블이 생기는게 아니라, members 테이블의 email 컬럼으로 저장됨
 * - Member 엔티티와 생명주기를 같이함 (독립적으로 존재 불가)
 */
@Embeddable
data class Email(
    /**
     * [실제 저장되는 값]
     * - members 테이블의 email 컬럼에 저장됨
     * - @Embeddable 덕분에 별도 테이블 생성 없이 Member 테이블에 포함됨
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private val value: String
) {
    /**
     * [init 블록: Value Object의 핵심]
     *
     * MVC 방식:
     * ```
     * member.setEmail("invalid-email")  // 잘못된 값도 할당됨
     * // 나중에 어디선가 검증 필요...
     * ```
     *
     * DDD 방식:
     * ```
     * val email = Email("invalid-email")  // 여기서 즉시 예외 발생!
     * // 잘못된 Email 객체는 애초에 생성될 수 없음
     * ```
     *
     * 이것이 "올바른 도메인 객체만 존재할 수 있다"는 DDD의 불변성 원칙
     */
    init {
        require(value.isNotBlank()) { "이메일은 필수입니다" }
        require(value.matches(EMAIL_REGEX)) { "올바른 이메일 형식이 아닙니다: $value" }
        require(value.length <= 100) { "이메일은 100자를 초과할 수 없습니다" }
    }

    /**
     * [Value Object의 불변성]
     *
     * - val로 선언되어 있어 값 변경 불가
     * - setter가 없음 (data class이지만 val이므로 copy()만 가능)
     *
     * 변경이 필요하면?
     * ```
     * // ❌ 불가능
     * email.value = "new@example.com"
     *
     * // ✅ 새로운 Email 객체 생성
     * val newEmail = Email("new@example.com")
     * member.changeEmail(newEmail)
     * ```
     */
    fun getValue(): String = value

    /**
     * [동일성 비교]
     *
     * Value Object는 "값이 같으면 같은 객체"
     *
     * Entity (Member):
     * - member1.id == 1, email="a@test.com"
     * - member2.id == 2, email="a@test.com"
     * - member1 != member2  (ID가 다르므로 다른 회원)
     *
     * Value Object (Email):
     * - email1 = Email("a@test.com")
     * - email2 = Email("a@test.com")
     * - email1 == email2  (값이 같으므로 동일한 이메일)
     *
     * data class를 사용하면 자동으로 equals/hashCode가 값 기반으로 구현됨
     */

    override fun toString(): String = value

    companion object {
        /**
         * [도메인 규칙을 상수로 명시]
         *
         * MVC: 검증 정규식이 Controller, Service 등 여러 곳에 복붙됨
         * DDD: 도메인 규칙이 Value Object 내부에 한 곳에만 존재
         */
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        /**
         * [정적 팩토리 메서드 패턴 (선택사항)]
         *
         * 생성자 대신 의미있는 이름의 메서드로 객체 생성 가능
         *
         * 사용 예:
         * ```
         * val email = Email.of("user@example.com")
         * val nullableEmail = Email.ofNullable(null)  // null 허용하는 경우
         * ```
         */
        fun of(value: String): Email = Email(value)

        /**
         * [Null-safe 생성 (optional)]
         *
         * 이메일이 선택사항인 경우에 사용
         * 예: 회원가입 시 이메일 입력은 필수이지만, 임시 회원은 이메일 없을 수도 있음
         */
        fun ofNullable(value: String?): Email? = value?.let { Email(it) }
    }
}

/**
 * [Value Object 핵심 정리]
 *
 * 1. 불변 객체 (val, setter 없음)
 * 2. 값 기반 동등성 (data class 사용)
 * 3. 자체 검증 (init 블록에서 검증)
 * 4. 식별자 없음 (ID 없이 값만 중요)
 * 5. 부모 엔티티에 종속 (@Embeddable)
 *
 * [언제 Value Object를 만드는가?]
 *
 * ✅ Value Object로 만들어야 할 것들:
 * - Email, PhoneNumber (형식 검증 필요)
 * - Money, Price (금액 계산 로직 필요)
 * - Address (주소 구성 요소 묶음)
 * - DateRange (시작일~종료일 검증)
 * - Password (암호화, 정책 검증)
 *
 * ❌ 단순 String으로 둬도 되는 것들:
 * - name (단순 문자열, 특별한 규칙 없음)
 * - description (자유 텍스트)
 *
 * 기준: "특별한 비즈니스 규칙이나 검증이 필요한가?" → Yes면 Value Object
 */
