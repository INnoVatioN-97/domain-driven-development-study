package com.innovation.dddexample.domain.member.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Member Aggregate Root
 *
 * [DDD vs MVC 차이점]
 * MVC: Member는 단순히 DB 테이블을 매핑하는 "데이터 구조체"에 불과함 (Anemic Model)
 *      - getter/setter만 있고 비즈니스 로직은 Service에 존재
 *      - 데이터와 행동이 분리되어 있음
 *
 * DDD: Member는 "행동을 가진 도메인 객체"이자 Aggregate의 Root
 *      - 자신의 상태를 변경하는 비즈니스 로직을 직접 포함 (Rich Model)
 *      - 불변성과 일관성을 스스로 보장
 *      - 외부에서 내부 상태를 함부로 변경할 수 없도록 캡슐화
 */
@Entity
@Table(name = "members") // "member"는 MySQL 예약어이므로 복수형 사용
class Member(
    /**
     * [DDD 특징]
     * ID는 도메인의 정체성(Identity)을 나타냄
     * - MVC에서는 그냥 "PK"이지만, DDD에서는 "이 엔티티를 식별하는 유일한 값"이라는 의미
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /**
     * [DDD: Value Object 패턴 적용]
     *
     * MVC 방식:
     * ```
     * @Column
     * var email: String  // 단순 문자열, 검증 없음
     * ```
     *
     * DDD 방식:
     * ```
     * @Embedded
     * var email: Email  // Value Object, 자체 검증 포함
     * ```
     *
     * [Aggregate와 Value Object의 관계]
     * - Member Aggregate는 Email, PhoneNumber Value Object를 "소유"함
     * - Value Object는 독립적으로 존재할 수 없음 (Member의 일부)
     * - @Embedded: JPA에게 "별도 테이블 말고 같은 테이블에 컬럼으로 넣어라"고 지시
     *
     * DB 테이블 구조:
     * members {
     *     id,
     *     email,        -- Email Value Object의 value 필드
     *     phone_number, -- PhoneNumber Value Object의 value 필드
     *     name
     * }
     *
     * [이게 바로 Aggregate 패턴!]
     * Member(Aggregate Root) + Email + PhoneNumber = 하나의 Aggregate
     * - 외부에서는 Member를 통해서만 접근
     * - Email, PhoneNumber는 Member 없이 독립적으로 존재 불가
     * - 트랜잭션 일관성 단위 = Member Aggregate 전체
     */
    @Embedded
    var email: Email,

    /**
     * [DDD: 불변성 원칙]
     * MVC: 모든 필드가 public setter로 노출되어 어디서든 변경 가능
     * DDD: var로 선언하되, 변경은 반드시 도메인 메서드를 통해서만 허용
     *      - 예: member.name = "New Name" ❌
     *      - 예: member.updateProfile(name, phoneNumber) ✅
     *
     * 이를 통해 "왜 이름이 변경되는가?"에 대한 비즈니스 컨텍스트를 코드에 명시
     */
    @Column(nullable = false, length = 50)
    var name: String,

    @Column(nullable = false, length = 128)
    var password: String,

    /**
     * [Value Object 적용]
     *
     * 기존: var phoneNumber: String
     * 개선: var phoneNumber: PhoneNumber
     *
     * 이제 전화번호 검증, 형식 변환, 마스킹 등의 로직이
     * Member가 아닌 PhoneNumber 자체에 존재함
     *
     * → 책임 분리, 재사용성 향상
     */
    @Embedded
    var phoneNumber: PhoneNumber,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val role: Role = Role.USER,

    /**
     * [DDD: 도메인 이벤트 추적]
     * MVC: 등록일은 그냥 "언제 INSERT 되었나" 정도의 의미
     * DDD: "회원이 언제 우리 시스템에 가입했는가"라는 도메인 이벤트를 기록
     */
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * [DDD: Soft Delete 패턴]
     * 회원 탈퇴는 단순 DELETE가 아닌 "탈퇴했다"는 상태 변경으로 처리
     * - 도메인 이벤트: "회원이 탈퇴했다"
     * - 이력 관리 및 복구 가능성 확보
     */
    @Column(nullable = true)
    var deletedAt: LocalDateTime? = null
) {

    init {
        require(name.isNotBlank()) { "회원 이름은 필수입니다" }
    }

    /**
     * [DDD 핵심: 도메인 로직을 엔티티 내부에 캡슐화]
     *
     * MVC 패턴:
     * ```
     * // MemberService.java
     * public void updateProfile(Member member, String name, String phoneNumber) {
     *     member.setName(name);
     *     member.setPhoneNumber(phoneNumber);
     *     memberRepository.save(member);
     * }
     * ```
     * - Service에 비즈니스 로직이 흩어져 있음
     * - Member는 데이터 컨테이너일 뿐
     *
     * DDD 패턴:
     * ```
     * // Application Service
     * member.updateProfile(name, phoneNumber)
     * memberRepository.save(member)
     * ```
     * - 비즈니스 로직이 도메인 객체 안에 있음
     * - "회원이 자신의 프로필을 수정한다"는 의미가 코드에 명확히 드러남
     */
    /**
     * [Value Object 사용 후 개선된 메서드]
     *
     * 기존 (String 사용):
     * ```
     * fun updateProfile(name: String, phoneNumber: String) {
     *     // 여기서 전화번호 형식 검증 필요
     *     require(phoneNumber.matches(Regex(...))) { "형식 오류" }
     *     this.phoneNumber = phoneNumber
     * }
     * ```
     *
     * 개선 (Value Object 사용):
     * ```
     * fun updateProfile(name: String, phoneNumber: PhoneNumber) {
     *     // 검증 불필요! PhoneNumber 객체가 존재한다 = 이미 유효하다
     *     this.phoneNumber = phoneNumber
     * }
     * ```
     *
     * [핵심 포인트]
     * - PhoneNumber 타입 자체가 "유효한 전화번호"를 보장
     * - 검증 로직 중복 제거
     * - Member는 "프로필 업데이트"라는 비즈니스 로직에만 집중
     */
    fun updateProfile(name: String, phoneNumber: PhoneNumber) {
        require(name.isNotBlank()) { "회원 이름은 필수입니다" }

        this.name = name
        this.phoneNumber = phoneNumber
    }

    /**
     * [DDD: 도메인 이벤트를 명시적으로 표현]
     *
     * MVC: memberRepository.delete(member) 또는 member.setDeleted(true)
     *      - "삭제"라는 기술적 표현
     *
     * DDD: member.withdraw()
     *      - "탈퇴하다"라는 도메인 언어(유비쿼터스 언어) 사용
     *      - 비즈니스 의도가 명확히 드러남
     */
    fun withdraw() {
        // [DDD: 비즈니스 불변성 보호]
        // 이미 탈퇴한 회원은 재탈퇴 불가
        require(deletedAt == null) { "이미 탈퇴한 회원입니다" }

        this.deletedAt = LocalDateTime.now()

        // [DDD: 도메인 이벤트 발행 가능]
        // 실전에서는 여기서 MemberWithdrawnEvent 같은 도메인 이벤트를 발행하여
        // 다른 Bounded Context(예: 예매 취소, 포인트 정산 등)에 알릴 수 있음
    }

    /**
     * [DDD: 상태 조회도 도메인 의미를 담아 표현]
     *
     * MVC: if (member.getWithdrawnAt() != null) { ... }
     *      - 기술적 표현
     *
     * DDD: if (member.isWithdrawn()) { ... }
     *      - 도메인 언어로 표현하여 가독성 향상
     */
    fun isWithdrawn(): Boolean = deletedAt != null

    fun isActive(): Boolean = !isWithdrawn()

    /**
     * [DDD: 이메일 변경 - Value Object 활용]
     *
     * 기존 (String):
     * ```
     * fun changeEmail(newEmail: String) {
     *     require(newEmail.matches(Regex(...))) { "형식 오류" }  // 중복 검증
     *     require(newEmail != this.email) { "동일" }
     *     this.email = newEmail
     * }
     * ```
     *
     * 개선 (Value Object):
     * ```
     * fun changeEmail(newEmail: Email) {
     *     require(newEmail != this.email) { "동일" }  // 비즈니스 규칙만 검증
     *     this.email = newEmail
     * }
     * ```
     *
     * [Aggregate Root의 책임]
     * - Email 형식 검증: Email Value Object가 담당 ✅
     * - "현재 이메일과 다른지" 검증: Member Aggregate가 담당 ✅
     * - 각자의 책임이 명확히 분리됨
     */
    fun changeEmail(newEmail: Email) {
        require(newEmail != this.email) { "현재 이메일과 동일합니다" }
        this.email = newEmail
    }

    /**
     * [DDD vs MVC 정리]
     *
     * MVC (Anemic Domain Model):
     * - Entity: 데이터 보관함 (getter/setter)
     * - Service: 모든 비즈니스 로직 포함
     * - 데이터와 로직이 분리 → 응집도 낮음
     *
     * DDD (Rich Domain Model):
     * - Entity: 데이터 + 비즈니스 로직 + 불변성 보장
     * - Application Service: 유스케이스 조율 (트랜잭션, 도메인 객체 협업)
     * - Domain Service: 여러 Aggregate를 넘나드는 도메인 로직
     * - 데이터와 로직이 함께 → 응집도 높음, 캡슐화 강화
     *
     * 핵심 차이:
     * 1. MVC: memberService.updateName(member, "new name")
     *    DDD: member.updateProfile("new name", phoneNumber)
     *
     * 2. MVC: 비즈니스 규칙이 Service 레이어 전체에 흩어짐
     *    DDD: 비즈니스 규칙이 도메인 객체 내부에 응집됨
     *
     * 3. MVC: DB 중심 설계 (테이블 → Entity)
     *    DDD: 도메인 중심 설계 (비즈니스 개념 → Aggregate)
     */
}
