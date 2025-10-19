package com.innovation.dddexample.domain.member.exception

import com.innovation.dddexample.domain.common.exception.BusinessRuleViolationException

/**
 * 탈퇴한 회원이 로그인 시도 시 발생하는 예외
 *
 * [비즈니스 규칙]
 * - 탈퇴한 회원(deletedAt != null)은 로그인할 수 없음
 * - HTTP 403 Forbidden 또는 400 Bad Request로 처리
 *
 * [사용 케이스]
 * - 로그인 시도 시 회원이 탈퇴 상태인 경우
 */
class WithdrawnMemberException(email: String) : BusinessRuleViolationException(
    "탈퇴한 회원입니다: $email"
)
