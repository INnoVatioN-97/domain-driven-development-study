package com.innovation.dddexample.domain.member.exception

import com.innovation.dddexample.domain.common.exception.BusinessRuleViolationException

/**
 * 로그인 시 비밀번호가 일치하지 않을 때 발생하는 예외입니다.
 */
class InvalidPasswordException(
    message: String = "Password does not match"
) : BusinessRuleViolationException(message)
