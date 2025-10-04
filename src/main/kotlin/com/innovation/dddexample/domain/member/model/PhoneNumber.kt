package com.innovation.dddexample.domain.member.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * PhoneNumber Value Object
 *
 * [Value Object의 실전 활용 예시]
 *
 * MVC 방식:
 * ```
 * // Controller
 * if (!phoneNumber.matches("010-\\d{4}-\\d{4}")) {
 *     throw new Exception("전화번호 형식 오류");
 * }
 *
 * // Service
 * if (!phoneNumber.matches("010-\\d{4}-\\d{4}")) {  // 중복!
 *     throw new Exception("전화번호 형식 오류");
 * }
 * ```
 * - 검증 로직이 여러 곳에 중복
 * - 형식이 바뀌면 모든 곳을 수정해야 함
 *
 * DDD 방식:
 * ```
 * val phoneNumber = PhoneNumber("010-1234-5678")  // 여기서만 검증!
 * // 이후로는 PhoneNumber 타입만 믿고 사용
 * ```
 * - 검증 로직이 PhoneNumber 내부 한 곳에만 존재
 * - PhoneNumber 객체가 존재한다 = 유효한 전화번호다 (보장됨)
 */
@Embeddable
data class PhoneNumber(
    /**
     * [정규화된 형식으로 저장]
     * - "01012345678" 입력 → "010-1234-5678" 변환 후 저장
     * - DB에는 일관된 형식으로 저장되어 검색/비교 용이
     */
    @Column(name = "phone_number", nullable = false, length = 20)
    private val value: String
) {
    /**
     * [생성자에서 정규화 + 검증]
     *
     * Value Object의 강력함:
     * 1. 다양한 입력 형식 허용 ("01012345678", "010-1234-5678")
     * 2. 내부적으로 정규화 (하이픈 추가)
     * 3. 검증 (형식 체크)
     * 4. 저장 (정규화된 형식으로)
     *
     * → 외부에서는 이 모든 복잡함을 신경 쓸 필요 없음!
     */
    init {
        val normalized = normalizePhoneNumber(value)
        require(normalized.isNotBlank()) { "전화번호는 필수입니다" }
        require(normalized.matches(PHONE_REGEX)) {
            "올바른 전화번호 형식이 아닙니다: $value (형식: 010-1234-5678)"
        }
    }

    /**
     * [Value Object의 getter]
     *
     * - private val이므로 외부에서 직접 접근 불가
     * - getValue() 메서드를 통해서만 조회 가능
     * - 이것도 불변성을 보장하는 방법 중 하나
     */
    fun getValue(): String = value

    /**
     * [도메인 로직: 전화번호 마스킹]
     *
     * Value Object는 단순 데이터 저장소가 아님!
     * 해당 값과 관련된 "비즈니스 로직"도 포함할 수 있음
     *
     * MVC 방식:
     * ```
     * // Utils 클래스 어딘가에...
     * public static String maskPhoneNumber(String phone) {
     *     return phone.replaceAll("(\\d{3})-(\\d{4})-(\\d{4})", "$1-****-$2");
     * }
     * ```
     *
     * DDD 방식:
     * ```
     * val masked = phoneNumber.toMasked()  // 전화번호가 스스로 마스킹함
     * ```
     *
     * 장점: 전화번호 관련 로직이 PhoneNumber 클래스에 응집됨
     */
    fun toMasked(): String {
        // "010-1234-5678" → "010-****-5678"
        return value.replaceRange(4, 8, "****")
    }

    /**
     * [도메인 로직: 통신사 구분]
     *
     * 전화번호로부터 통신사를 판별하는 것도 "전화번호 도메인의 책임"
     *
     * MVC: 이런 로직이 Service 어딘가에 흩어져 있음
     * DDD: PhoneNumber가 스스로 통신사를 알고 있음
     */
    fun getCarrier(): String {
        return when {
            value.startsWith("010") -> "알 수 없음" // 010은 통신사 구분 불가
            value.startsWith("011") -> "SKT"
            value.startsWith("016") -> "KT"
            value.startsWith("019") -> "LG U+"
            else -> "기타"
        }
    }

    override fun toString(): String = value

    companion object {
        /**
         * [도메인 규칙 명시]
         * - 전화번호 형식: 010-XXXX-XXXX
         * - 나중에 휴대폰 외 일반전화도 허용하려면 여기만 수정하면 됨
         */
        private val PHONE_REGEX = Regex("^01[0-9]-\\d{3,4}-\\d{4}$")

        /**
         * [정규화 로직]
         *
         * 다양한 입력 형식을 표준 형식으로 변환:
         * - "01012345678" → "010-1234-5678"
         * - "010 1234 5678" → "010-1234-5678"
         * - "010-1234-5678" → "010-1234-5678" (그대로)
         *
         * [왜 정규화가 중요한가?]
         * 1. DB 저장: 일관된 형식으로 저장되어야 검색/비교 가능
         * 2. 비교 연산: "01012345678" == "010-1234-5678" 를 같다고 판단
         * 3. 사용자 편의: 사용자는 하이픈 없이 입력해도 됨
         */
        private fun normalizePhoneNumber(input: String): String {
            // 1. 하이픈, 공백 모두 제거
            val digitsOnly = input.replace(Regex("[^0-9]"), "")

            // 2. 11자리 숫자인지 확인
            if (digitsOnly.length != 11) {
                return input // 원본 반환 (검증에서 실패하게 함)
            }

            // 3. 010-XXXX-XXXX 형식으로 변환
            return "${digitsOnly.substring(0, 3)}-${digitsOnly.substring(3, 7)}-${digitsOnly.substring(7)}"
        }

        /**
         * [정적 팩토리 메서드]
         */
        fun of(value: String): PhoneNumber = PhoneNumber(value)

        /**
         * [다양한 생성 방식 제공]
         *
         * 실전에서는 이렇게 여러 팩토리 메서드를 제공할 수 있음:
         * ```
         * PhoneNumber.of("010-1234-5678")           // 기본
         * PhoneNumber.fromDigitsOnly("01012345678") // 숫자만
         * PhoneNumber.fromInternational("+82-10-1234-5678") // 국제번호
         * ```
         */
        fun fromDigitsOnly(digits: String): PhoneNumber {
            require(digits.matches(Regex("^[0-9]{11}$"))) {
                "11자리 숫자만 입력 가능합니다"
            }
            return PhoneNumber(digits)
        }
    }
}

/**
 * [Value Object의 또 다른 예: 주소 (Address)]
 *
 * 전화번호처럼 여러 필드를 묶어서 하나의 Value Object로 만들 수도 있음:
 *
 * ```kotlin
 * @Embeddable
 * data class Address(
 *     @Column(name = "zip_code")
 *     val zipCode: String,
 *
 *     @Column(name = "street")
 *     val street: String,
 *
 *     @Column(name = "city")
 *     val city: String
 * ) {
 *     fun getFullAddress(): String = "$city $street ($zipCode)"
 * }
 * ```
 *
 * Member에서 사용:
 * ```kotlin
 * @Embedded
 * var address: Address
 * ```
 *
 * DB 테이블:
 * members {
 *     id,
 *     email,
 *     zip_code,    // Address의 zipCode
 *     street,      // Address의 street
 *     city         // Address의 city
 * }
 * ```
 */

/**
 * [핵심 정리: Value Object가 해결하는 문제]
 *
 * 1. **검증 로직 중복 제거**
 *    - MVC: Controller, Service 여러 곳에서 같은 검증
 *    - DDD: Value Object 생성자에서 단 한 번만 검증
 *
 * 2. **타입 안전성**
 *    - MVC: String phoneNumber → 실수로 이메일 넣을 수도 있음
 *    - DDD: PhoneNumber phoneNumber → 컴파일 타임에 타입 체크
 *
 * 3. **도메인 로직 응집**
 *    - MVC: 전화번호 관련 로직이 Utils, Service 등 여기저기 흩어짐
 *    - DDD: PhoneNumber 클래스 안에 모든 로직 응집
 *
 * 4. **불변성 보장**
 *    - MVC: member.setPhoneNumber("잘못된값") 언제든 가능
 *    - DDD: 생성 시점에만 검증, 이후 변경 불가
 *
 * 5. **도메인 언어 명확화**
 *    - MVC: String phoneNumber (그냥 문자열)
 *    - DDD: PhoneNumber phoneNumber (전화번호라는 도메인 개념)
 */
