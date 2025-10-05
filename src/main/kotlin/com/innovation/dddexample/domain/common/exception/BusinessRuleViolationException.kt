package com.innovation.dddexample.domain.common.exception

/**
 * 비즈니스 규칙 위반 시 발생하는 예외입니다.
 *
 * [사용 케이스]
 * - 예매 취소 불가 (공연 시작 1시간 전)
 * - 회원 탈퇴 불가 (진행 중인 예매 존재)
 * - 좌석 예약 불가 (이미 예약됨)
 *
 * [HTTP 매핑]
 * - GlobalExceptionHandler에서 400 Bad Request로 변환
 *
 * [구체 예외 예시]
 * - CannotCancelReservationException("공연 시작 1시간 전에는 취소할 수 없습니다")
 * - CannotWithdrawMemberException("진행 중인 예매가 있어 탈퇴할 수 없습니다")
 * - SeatAlreadyReservedException("이미 예약된 좌석입니다")
 *
 * @param message 비즈니스 규칙 위반 설명
 */
abstract class BusinessRuleViolationException(message: String) : DomainException(message)
