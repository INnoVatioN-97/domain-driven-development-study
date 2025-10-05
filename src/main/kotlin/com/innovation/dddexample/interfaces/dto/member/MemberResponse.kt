package com.innovation.dddexample.interfaces.dto.member

import com.innovation.dddexample.domain.member.model.Member
import java.time.LocalDateTime

/**
 * 회원 정보 조회 API의 응답 DTO입니다.
 *
 * [DDD 패턴: DTO (Data Transfer Object)]
 * - 도메인 엔티티(Member)를 직접 노출하지 않음
 * - API 계약(Contract)을 안정적으로 유지
 * - 도메인 모델 변경이 API 응답 형식에 자동으로 영향을 주지 않음
 *
 * [왜 도메인 엔티티를 직접 반환하지 않나?]
 * 1. 캡슐화: 내부 구조 노출 방지 (JPA 어노테이션, 양방향 관계 등)
 * 2. 보안: 민감 정보 제어 (phoneNumber.masked 사용)
 * 3. 안정성: 도메인 변경이 API에 영향 주지 않도록 분리
 * 4. 유연성: API 응답 형식을 독립적으로 설계 가능
 *
 * [Value Object 활용]
 * - email: Email.value 사용 (검증된 형식)
 * - phoneNumber: PhoneNumber.masked 사용 (프라이버시 보호!)
 */
data class MemberResponse(
    val id: Long,
    val name: String,
    val email: String,
    val phoneNumber: String,  // 마스킹된 형식: "010-****-5678"
    val status: String,       // "ACTIVE" or "WITHDRAWN"
    val pointBalance: Int,    // TODO: Points Aggregate 연동 시 실제 값 사용
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime  // TODO: Member 엔티티에 updatedAt 필드 추가 시 사용
)

/**
 * Member 엔티티를 MemberResponse DTO로 변환하는 확장 함수입니다.
 *
 * [Kotlin Idiom: Extension Function]
 * - member.toResponse() 형태로 자연스럽게 호출
 * - Mapper 클래스 불필요 (간단한 변환의 경우)
 * - DTO 파일에 함께 위치하여 응집도 향상
 *
 * [프라이버시 패턴]
 * - phoneNumber.masked 사용으로 "010-****-5678" 형식 반환
 * - Value Object가 제공하는 기능 활용
 * - DTO에서 마스킹 로직 중복 구현 불필요
 */
fun Member.toResponse(): MemberResponse = MemberResponse(
    id = this.id!!,
    name = this.name,
    email = this.email.value,  // Email Value Object의 value 직접 접근
    phoneNumber = this.phoneNumber.toMasked(),  // [중요] Privacy! 전체 번호가 아닌 마스킹된 형식
    status = if (this.isWithdrawn()) "WITHDRAWN" else "ACTIVE",
    pointBalance = 0,  // TODO: Points Aggregate 연동 필요
    createdAt = this.registeredAt,
    updatedAt = this.registeredAt  // TODO: updatedAt 필드 추가 필요
)
