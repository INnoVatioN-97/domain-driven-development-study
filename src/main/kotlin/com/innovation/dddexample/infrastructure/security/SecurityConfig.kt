package com.innovation.dddexample.infrastructure.security

import com.innovation.dddexample.infrastructure.security.filter.JwtAuthenticationFilter
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Spring Security 설정 클래스입니다.
 *
 * [인증 전략]
 * - JWT 기반 Stateless 인증
 * - 세션 사용 안함 (STATELESS)
 * - Bearer Token 방식
 *
 * [보안 정책]
 * - CSRF 비활성화 (JWT 사용으로 불필요)
 * - CORS 설정 (향후 프론트엔드 연동 시)
 * - 인증 없이 접근 가능한 경로 설정
 *
 * [필터 체인]
 * ```
 * HTTP Request
 *     ↓
 * JwtAuthenticationFilter (JWT 토큰 검증)
 *     ↓
 * SecurityContext 설정
 *     ↓
 * Controller (인증된 사용자 정보 사용)
 * ```
 */
@Configuration
@EnableWebSecurity
@ConfigurationPropertiesScan("com.innovation.dddexample.infrastructure.security.jwt")
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    /**
     * Spring Security 필터 체인을 설정합니다.
     *
     * [주요 설정]
     * 1. CSRF 비활성화 (JWT 사용)
     * 2. 세션 미사용 (Stateless)
     * 3. 경로별 인증 규칙
     * 4. JWT 필터 등록
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF 비활성화 (JWT 사용으로 불필요)
            .csrf { it.disable() }

            // 세션 미사용 (Stateless)
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // 경로별 인증 규칙
            .authorizeHttpRequests { auth ->
                auth
                    // 인증 없이 접근 가능한 경로
                    .requestMatchers(
                        "/auth/**",        // 로그인, 회원가입 등
//                        "/games/**",        // 경기
                        "/public/**",      // 공개 API
                        "/actuator/health"     // Health check
                    ).permitAll()

                    // 나머지 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            }

            // JWT 필터 등록 (UsernamePasswordAuthenticationFilter 이전)
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }

    /**
     * 비밀번호 암호화 인코더를 설정합니다.
     *
     * [BCrypt]
     * - 단방향 암호화 (복호화 불가)
     * - Salt 자동 생성
     * - 비밀번호 검증은 BCryptPasswordEncoder.matches() 사용
     *
     * [사용처]
     * - 회원 가입 시 비밀번호 암호화
     * - 로그인 시 비밀번호 검증
     *
     * [현재]
     * - Member 엔티티에 password 필드 없음
     * - 향후 로그인 기능 추가 시 사용
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
