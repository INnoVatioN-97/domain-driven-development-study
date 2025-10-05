package com.innovation.dddexample.domain.common.exception

/**
 * 엔티티 또는 Aggregate를 찾을 수 없을 때 발생하는 예외입니다.
 *
 * [사용 케이스]
 * - Member, Reservation, Performance 등 Aggregate Root를 ID로 조회했으나 존재하지 않음
 * - Repository.findById() 결과가 null일 때
 *
 * [HTTP 매핑]
 * - GlobalExceptionHandler에서 404 Not Found로 변환
 *
 * [구체 예외 예시]
 * - MemberNotFoundException(memberId)
 * - ReservationNotFoundException(reservationId)
 * - PerformanceNotFoundException(performanceId)
 *
 * @param message "Member not found with id: 123" 같은 구체적 메시지
 */
abstract class NotFoundException(message: String) : DomainException(message)
