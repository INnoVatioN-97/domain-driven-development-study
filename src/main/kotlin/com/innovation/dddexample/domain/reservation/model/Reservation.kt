package com.innovation.dddexample.domain.reservation.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 예매 애그리거트 루트
 */
@Entity
@Table(name = "reservations")
class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // 어느 회원의 예매인지 (Member 애그리거트 ID 참조)
    @Column(nullable = false)
    val memberId: Long,

    // 어느 경기에 대한 예매인지 (Game 애그리거트 ID 참조)
    @Column(nullable = false)
    val gameId: Long,

    // 예매 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ReservationStatus,

    // 예매 생성 일시
    @Column(nullable = false)
    val reservedAt: LocalDateTime = LocalDateTime.now(),
) {
    // 예매와 관련된 비즈니스 로직 (예: 취소, 상태 변경 등)이 여기에 위치하게 됩니다.
}
