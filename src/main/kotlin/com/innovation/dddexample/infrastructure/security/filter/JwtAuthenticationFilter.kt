package com.innovation.dddexample.infrastructure.security.filter

import com.innovation.dddexample.infrastructure.security.jwt.JwtTokenProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val logger = KotlinLogging.logger {}

/**
 * JWT 토큰 기반 인증 필터입니다.
 *
 * [역할]
 * - HTTP 요청에서 JWT 토큰 추출
 * - 토큰 유효성 검증
 * - 유효한 토큰이면 SecurityContext에 인증 정보 설정
 *
 * [처리 흐름]
 * ```
 * 1. Authorization 헤더에서 Bearer 토큰 추출
 * 2. 토큰 유효성 검증 (JwtTokenProvider.validateToken)
 * 3. 토큰에서 인증 정보 추출 (memberId, authorities)
 * 4. SecurityContext에 Authentication 설정
 * 5. 다음 필터로 진행
 * ```
 *
 * [OncePerRequestFilter]
 * - 요청당 한 번만 실행 보장
 * - Async 요청에서도 안전
 *
 * [SecurityContext]
 * - Spring Security가 현재 인증된 사용자 정보를 저장하는 곳
 * - ThreadLocal 기반 (요청당 독립적)
 * - Controller에서 @AuthenticationPrincipal로 접근 가능
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    /**
     * 모든 HTTP 요청마다 실행되는 필터 로직입니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 다음 필터 체인
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            val token = resolveToken(request)

            // 2. 토큰이 있고 유효하면 인증 정보 설정
            if (token != null && jwtTokenProvider.validateToken(token)) {
                val authentication = jwtTokenProvider.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = authentication

                logger.debug { "Set authentication for member: ${authentication.principal}" }
            } else if (token != null) {
                logger.warn("Invalid JWT token in request: ${request.requestURI}")
            }
        } catch (e: Exception) {
            logger.error { "Cannot set user authentication in SecurityContext: ${e.message}" }
            // 예외 발생해도 필터 체인 계속 진행
            // SecurityContext에 인증 정보 없으면 Spring Security가 401 응답
        }

        // 3. 다음 필터로 진행
        filterChain.doFilter(request, response)
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출합니다.
     *
     * @param request HTTP 요청
     * @return JWT 토큰 문자열 또는 null
     *
     * [추출 방법]
     * - Authorization 헤더 확인
     * - "Bearer " 접두사 제거
     * - 순수 토큰 문자열 반환
     *
     * [예시]
     * ```
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * → eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * ```
     */
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)

        return if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }
}
