package com.innovation.dddexample.domain.member.exception

import com.innovation.dddexample.domain.common.exception.DuplicateException

/**
 * 회원 가입 시 이메일이 중복될 때 발생하는 예외입니다.
 *
 * @param email 중복된 이메일 주소
 */
class DuplicateEmailException(email: String) : DuplicateException("Email already exists: $email")
