package com.innovation.dddexample.interfaces.rest.member

import com.innovation.dddexample.application.member.MemberQueryService
import com.innovation.dddexample.domain.member.exception.MemberNotFoundException
import com.innovation.dddexample.interfaces.dto.member.MemberResponse
import com.innovation.dddexample.interfaces.dto.member.toResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 회원 정보 조회 REST API 컨트롤러입니다.
 *
 * [DDD 패턴: Interface Layer (REST Controller)]
 * - HTTP 요청/응답 처리만 담당 (Thin Controller)
 * - 비즈니스 로직은 Application Service에 위임
 * - 도메인 예외를 HTTP 상태 코드로 변환
 *
 * [Controller의 책임]
 * 1. HTTP 요청 파싱 (@PathVariable, @RequestBody 등)
 * 2. Application Service 호출
 * 3. 도메인 엔티티 → DTO 변환
 * 4. HTTP 응답 생성 (상태 코드, 헤더, 바디)
 * 5. 예외 → HTTP 에러 응답 변환
 *
 * [Controller가 하지 말아야 할 것]
 * ❌ 비즈니스 로직 수행
 * ❌ 직접 Repository 호출
 * ❌ 트랜잭션 관리 (Service 계층 책임)
 * ❌ 도메인 엔티티 직접 반환
 */
@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberQueryService: MemberQueryService
) {
    /**
     * 회원 ID로 회원 정보를 조회합니다.
     *
     * [API Specification]
     * - Method: GET
     * - Path: /api/members/{id}
     * - Success: 200 OK with MemberResponse
     * - Error: 404 Not Found (회원 없음)
     *
     * @param id 조회할 회원의 고유 ID
     * @return MemberResponse DTO (Jackson이 자동으로 JSON 직렬화)
     *
     * [처리 흐름]
     * 1. Service 호출 → Member 엔티티 반환 (or MemberNotFoundException)
     * 2. Member → MemberResponse 변환 (toResponse() 확장 함수)
     * 3. Spring이 자동으로 JSON 직렬화하여 응답
     */
    @GetMapping("/{id}")
    fun getMember(@PathVariable id: Long): MemberResponse {
        val member = memberQueryService.getMemberById(id)
        return member.toResponse()
    }

    /**
     * MemberNotFoundException을 HTTP 404 Not Found로 변환합니다.
     *
     * [Exception Handling Pattern]
     * - 도메인 예외(MemberNotFoundException)는 도메인 언어
     * - Controller에서 HTTP 상태 코드로 변환
     * - 도메인 계층은 HTTP를 알 필요 없음
     *
     * [Spring Boot 통합]
     * - @ExceptionHandler: 특정 예외를 처리하는 메서드 지정
     * - Spring Boot의 기본 에러 응답 형식 활용
     *
     * @param ex 발생한 MemberNotFoundException
     * @return 404 Not Found 응답 with 에러 메시지
     */
    @ExceptionHandler(MemberNotFoundException::class)
    fun handleMemberNotFound(ex: MemberNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Member not found",
                    status = HttpStatus.NOT_FOUND.value()
                )
            )
    }
}

/**
 * API 에러 응답 DTO입니다.
 *
 * [간단한 에러 응답 형식]
 * - message: 사용자에게 보여줄 에러 메시지
 * - status: HTTP 상태 코드 (404, 400 등)
 *
 * [향후 확장 가능]
 * - timestamp: 에러 발생 시각
 * - path: 요청 경로
 * - errorCode: 애플리케이션 정의 에러 코드
 * - details: 상세 에러 정보 (필드 검증 오류 등)
 */
data class ErrorResponse(
    val message: String,
    val status: Int
)
