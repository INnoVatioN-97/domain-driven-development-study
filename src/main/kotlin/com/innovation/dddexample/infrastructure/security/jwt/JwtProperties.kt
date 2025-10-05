package com.innovation.dddexample.infrastructure.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * JWT 설정 프로퍼티입니다.
 *
 * [application.yml 매핑]
 * ```yaml
 * jwt:
 *   secret: your-secret-key-here-minimum-256-bits
 *   access-token-validity: 3600000      # 1시간 (밀리초)
 *   refresh-token-validity: 604800000   # 7일 (밀리초)
 * ```
 *
 * [@ConfigurationProperties]
 * - Spring Boot가 application.yml의 jwt.* 속성을 자동으로 바인딩
 * - @EnableConfigurationProperties 또는 @ConfigurationPropertiesScan 필요
 */
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    /**
     * JWT 서명에 사용할 비밀키
     *
     * [보안 요구사항]
     * - 최소 256비트 (32자 이상)
     * - 프로덕션에서는 환경변수로 관리
     * - 절대 Git에 커밋하지 말 것
     */
    val secret: String,

    /**
     * Access Token 유효 기간 (밀리초)
     *
     * [권장값]
     * - 개발: 1시간 (3600000ms)
     * - 프로덕션: 15분 (900000ms)
     */
    val accessTokenValidity: Long = 3600000L,  // 기본값: 1시간

    /**
     * Refresh Token 유효 기간 (밀리초)
     *
     * [권장값]
     * - 개발: 7일 (604800000ms)
     * - 프로덕션: 7-14일
     */
    val refreshTokenValidity: Long = 604800000L  // 기본값: 7일
)
