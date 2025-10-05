package com.innovation.dddexample.interfaces.rest.member

import com.innovation.dddexample.application.member.MemberQueryService
import com.innovation.dddexample.interfaces.dto.member.MemberResponse
import com.innovation.dddexample.interfaces.dto.member.toResponse
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
@RequestMapping("/members")
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
     * - Error: 404 Not Found (회원 없음) ← GlobalExceptionHandler에서 처리
     *
     * @param id 조회할 회원의 고유 ID
     * @return MemberResponse DTO (Jackson이 자동으로 JSON 직렬화)
     *
     * [처리 흐름]
     * 1. Service 호출 → Member 엔티티 반환 (or MemberNotFoundException)
     * 2. Member → MemberResponse 변환 (toResponse() 확장 함수)
     * 3. Spring이 자동으로 JSON 직렬화하여 응답
     *
     * [예외 처리]
     * - MemberNotFoundException 발생 시 GlobalExceptionHandler가 404로 변환
     * - Controller는 예외 처리 코드 없이 비즈니스 로직만 집중
     */
    @GetMapping("/{id}")
    fun getMember(@PathVariable id: Long): MemberResponse {
        val member = memberQueryService.getMemberById(id)
        return member.toResponse()
    }
}
