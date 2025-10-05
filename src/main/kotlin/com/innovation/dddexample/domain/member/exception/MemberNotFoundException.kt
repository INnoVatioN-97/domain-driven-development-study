package com.innovation.dddexample.domain.member.exception

import com.innovation.dddexample.domain.common.exception.NotFoundException

/**
 * Member를 찾을 수 없을 때 발생하는 도메인 예외입니다.
 *
 * [DDD 패턴: Domain Exception]
 * - NotFoundException을 상속하여 공통 예외 처리 가능
 * - 도메인 언어로 예외를 표현 ("MemberNotFound")
 * - HTTP 상태 코드(404) 같은 기술적 세부사항은 인터페이스 계층에서 처리
 * - 도메인 계층은 비즈니스 개념만 다룸
 *
 * [사용 위치]
 * - Application Service: memberRepository.findById(id) ?: throw MemberNotFoundException(id)
 * - GlobalExceptionHandler: NotFoundException → HTTP 404 Not Found로 변환
 *
 * [예외 처리 우선순위]
 * 1. MemberExceptionHandler (Member 도메인 특수 예외) - 현재는 없음
 * 2. GlobalExceptionHandler.handleNotFound() (NotFoundException 공통 처리) ← 여기서 처리
 */
class MemberNotFoundException(
    memberId: Long
) : NotFoundException("Member not found with id: $memberId")
