package com.innovation.dddexample.domain.member.exception

/**
 * Member를 찾을 수 없을 때 발생하는 도메인 예외입니다.
 *
 * [DDD 패턴: Domain Exception]
 * - 도메인 언어로 예외를 표현 ("MemberNotFound")
 * - HTTP 상태 코드(404) 같은 기술적 세부사항은 인터페이스 계층에서 처리
 * - 도메인 계층은 비즈니스 개념만 다룸
 *
 * [사용 위치]
 * - Application Service: memberRepository.findById(id) ?: throw MemberNotFoundException(id)
 * - Interface Layer (Controller): @ExceptionHandler로 404 Not Found로 변환
 */
class MemberNotFoundException(
    memberId: Long
) : RuntimeException("Member not found with id: $memberId")
