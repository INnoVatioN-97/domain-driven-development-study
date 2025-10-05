package com.innovation.dddexample.domain.common.exception

/**
 * 모든 도메인 예외의 최상위 추상 클래스입니다.
 *
 * [DDD Exception Hierarchy]
 * - 도메인 계층에서 발생하는 모든 예외는 이 클래스를 상속
 * - HTTP, REST 등 인프라스트럭처 개념과 무관
 * - 순수한 도메인 언어로 예외를 표현
 *
 * [설계 원칙]
 * - 도메인 예외는 비즈니스 규칙 위반을 표현
 * - HTTP 상태 코드는 Interface Layer에서 변환
 * - 예외 메시지는 도메인 용어 사용
 *
 * @param message 비즈니스 규칙 위반에 대한 설명 (도메인 언어)
 */
abstract class DomainException(message: String) : RuntimeException(message)
