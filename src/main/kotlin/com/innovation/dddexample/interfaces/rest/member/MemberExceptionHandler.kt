package com.innovation.dddexample.interfaces.rest.member

import com.innovation.dddexample.domain.member.exception.DuplicateEmailException
import com.innovation.dddexample.interfaces.dto.common.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

/**
 * Member 도메인 전용 예외 처리기입니다.
 *
 * [책임]
 * - Member 도메인에서만 발생하는 특수한 예외 처리
 * - 공통 예외(NotFoundException 등)는 GlobalExceptionHandler에서 처리
 * - Member API에서만 필요한 특별한 HTTP 상태 코드나 응답 형식 제공
 *
 * [@RestControllerAdvice(basePackages)]
 * - basePackages 지정으로 Member REST API에만 적용
 * - GlobalExceptionHandler보다 우선 적용됨 (더 구체적인 핸들러)
 *
 * [처리 우선순위]
 * ```
 * 1. MemberExceptionHandler (Member 도메인 전용)
 * 2. GlobalExceptionHandler (공통 예외)
 * 3. Spring Boot 기본 예외 처리
 * ```
 *
 * [예시: Member 도메인 특수 예외]
 * - MemberWithdrawalBlockedException → HTTP 423 Locked
 *   (진행 중인 예매가 있어 탈퇴 불가)
 * - MemberSuspendedException → HTTP 403 Forbidden
 *   (정지된 회원)
 * - MemberEmailNotVerifiedException → HTTP 403 Forbidden
 *   (이메일 미인증)
 *
 * [향후 확장]
 * - 현재는 Member 도메인에 특수 예외가 없어 비어있음
 * - 비즈니스 로직이 복잡해지면 여기에 추가
 */
@RestControllerAdvice(basePackages = ["com.innovation.dddexample.interfaces.rest.member"])
class MemberExceptionHandler {

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(ex: DuplicateEmailException): ResponseEntity<ErrorResponse> {
        logger.warn { "Duplicate email registration attempt: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.CONFLICT) // 409 Conflict
            .body(
                ErrorResponse(
                    message = ex.message ?: "Email already exists",
                    status = HttpStatus.CONFLICT.value()
                )
            )
    }

    /**
     * 향후 Member 도메인 특수 예외 처리 예시:
     *
     * @ExceptionHandler(MemberWithdrawalBlockedException::class)
     * fun handleWithdrawalBlocked(ex: MemberWithdrawalBlockedException): ResponseEntity<ErrorResponse> {
     *     logger.warn { "Member withdrawal blocked: ${ex.message}" }
     *     return ResponseEntity
     *         .status(HttpStatus.LOCKED) // 423 Locked
     *         .body(
     *             ErrorResponse(
     *                 message = ex.message ?: "Cannot withdraw: active reservations exist",
     *                 status = HttpStatus.LOCKED.value()
     *             )
     *         )
     * }
     *
     * @ExceptionHandler(MemberSuspendedException::class)
     * fun handleSuspended(ex: MemberSuspendedException): ResponseEntity<ErrorResponse> {
     *     logger.warn { "Suspended member tried to access: ${ex.message}" }
     *     return ResponseEntity
     *         .status(HttpStatus.FORBIDDEN) // 403 Forbidden
     *         .body(
     *             ErrorResponse(
     *                 message = "Your account is suspended",
     *                 status = HttpStatus.FORBIDDEN.value()
     *             )
     *         )
     * }
     */
}
