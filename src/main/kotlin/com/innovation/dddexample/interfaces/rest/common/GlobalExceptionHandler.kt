package com.innovation.dddexample.interfaces.rest.common

import com.innovation.dddexample.domain.common.exception.BusinessRuleViolationException
import com.innovation.dddexample.domain.common.exception.DuplicateException
import com.innovation.dddexample.domain.common.exception.NotFoundException
import com.innovation.dddexample.interfaces.dto.common.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

private val logger = KotlinLogging.logger {}

/**
 * 전역 공통 예외 처리기입니다.
 *
 * [책임]
 * - 모든 도메인에서 공통으로 발생하는 예외 처리
 * - NotFoundException, DuplicateException, BusinessRuleViolationException 등
 * - 일관된 HTTP 상태 코드 및 에러 응답 형식 제공
 *
 * [도메인별 특수 예외]
 * - Member 도메인의 특수한 예외: MemberExceptionHandler
 * - Reservation 도메인의 특수한 예외: ReservationExceptionHandler (향후)
 * - 도메인별 ExceptionHandler가 GlobalExceptionHandler보다 우선 적용됨
 *
 * [@RestControllerAdvice]
 * - 모든 @RestController에서 발생하는 예외를 중앙 집중식으로 처리
 * - 각 Controller에서 예외 처리 코드 중복 제거
 *
 * [Exception Handling Strategy]
 * ```
 * 1. 도메인별 특수 예외 → MemberExceptionHandler (basePackages 지정)
 * 2. 공통 도메인 예외 → GlobalExceptionHandler (NotFoundException 등)
 * 3. 예상치 못한 예외 → GlobalExceptionHandler (Exception.class)
 * ```
 *
 * [장점]
 * 1. 중앙 집중식 공통 예외 처리 (코드 중복 제거)
 * 2. 일관된 에러 응답 형식
 * 3. Controller를 Thin하게 유지
 * 4. 도메인별 특수 처리 가능 (도메인별 ExceptionHandler와 분리)
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * NotFoundException (공통) → HTTP 404 Not Found
     *
     * [처리 예외]
     * - MemberNotFoundException
     * - ReservationNotFoundException
     * - PerformanceNotFoundException
     * - 기타 모든 NotFoundException 하위 예외
     *
     * [처리 흐름]
     * 1. 어느 Controller에서든 NotFoundException 발생
     * 2. Spring이 이 핸들러를 찾아 자동 호출
     * 3. 404 Not Found 응답 반환
     */
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn { "Resource not found: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Resource not found",
                    status = HttpStatus.NOT_FOUND.value()
                )
            )
    }

    /**
     * DuplicateException (공통) → HTTP 409 Conflict
     *
     * [처리 예외]
     * - DuplicateEmailException
     * - DuplicatePhoneNumberException
     * - 기타 모든 DuplicateException 하위 예외
     *
     * [사용 케이스]
     * - 회원 가입 시 이메일 중복
     * - 유니크 제약 조건 위반
     */
    @ExceptionHandler(DuplicateException::class)
    fun handleDuplicate(ex: DuplicateException): ResponseEntity<ErrorResponse> {
        logger.warn { "Duplicate resource: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Resource already exists",
                    status = HttpStatus.CONFLICT.value()
                )
            )
    }

    /**
     * BusinessRuleViolationException (공통) → HTTP 400 Bad Request
     *
     * [처리 예외]
     * - CannotCancelReservationException
     * - CannotWithdrawMemberException
     * - SeatAlreadyReservedException
     * - 기타 비즈니스 규칙 위반
     *
     * [사용 케이스]
     * - 예매 취소 불가 (공연 시작 1시간 전)
     * - 회원 탈퇴 불가 (진행 중인 예매 존재)
     */
    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolation(ex: BusinessRuleViolationException): ResponseEntity<ErrorResponse> {
        logger.warn { "Business rule violation: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Business rule violation",
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )
    }

    /**
     * MethodArgumentTypeMismatchException (Spring) → HTTP 400 Bad Request
     *
     * [처리 대상]
     * - @PathVariable, @RequestParam 타입 변환 실패
     * - 예: GET /api/members/abc (Long 기대, String 전달)
     *
     * [사용 케이스]
     * - GET /api/members/{id} 에서 id에 숫자가 아닌 값 전달
     * - GET /api/reservations/{id} 등 모든 Long ID 파라미터
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        logger.warn { "Type mismatch for parameter '${ex.name}': ${ex.value}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    message = "Invalid parameter type: ${ex.name}",
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )
    }

    /**
     * IllegalArgumentException (Value Object 검증 실패 등) → HTTP 400 Bad Request
     *
     * [처리 대상]
     * - Value Object 생성 시 검증 실패 (Email, PhoneNumber 등)
     * - Domain 로직에서 발생하는 IllegalArgumentException
     *
     * [사용 케이스]
     * - Email("invalid-format") → "올바른 이메일 형식이 아닙니다"
     * - PhoneNumber("123") → "올바른 전화번호 형식이 아닙니다"
     * - Member.updateProfile(name = "") → "회원 이름은 필수입니다"
     *
     * [주의사항]
     * - IllegalArgumentException은 너무 범용적이므로 신중하게 사용
     * - 가능하면 도메인별 예외 클래스 사용 권장
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn { "Illegal argument: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Invalid request parameter",
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )
    }

    /**
     * Exception (최종 안전망) → HTTP 500 Internal Server Error
     *
     * [처리 대상]
     * - 예상치 못한 모든 예외
     * - NullPointerException, IllegalStateException 등
     *
     * [주의]
     * - 실제 에러 메시지는 로그로만 남기고, 클라이언트에는 일반 메시지 전달
     * - 보안상 내부 구현 세부사항 노출 방지
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Unexpected error occurred" }
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    message = "An unexpected error occurred",
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value()
                )
            )
    }
}
