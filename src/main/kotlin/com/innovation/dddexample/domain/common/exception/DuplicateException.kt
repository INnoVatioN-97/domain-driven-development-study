package com.innovation.dddexample.domain.common.exception

/**
 * 중복된 값이 존재할 때 발생하는 예외입니다.
 *
 * [사용 케이스]
 * - 이메일 중복 (회원 가입 시)
 * - 전화번호 중복
 * - 유니크 제약 조건 위반
 *
 * [HTTP 매핑]
 * - GlobalExceptionHandler에서 409 Conflict로 변환
 *
 * [구체 예외 예시]
 * - DuplicateEmailException(email)
 * - DuplicatePhoneNumberException(phoneNumber)
 *
 * @param message "Email already exists: test@example.com" 같은 구체적 메시지
 */
abstract class DuplicateException(message: String) : DomainException(message)
