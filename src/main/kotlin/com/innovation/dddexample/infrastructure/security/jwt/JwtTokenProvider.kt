package com.innovation.dddexample.infrastructure.security.jwt

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * JWT 토큰 생성 및 검증을 담당하는 핵심 컴포넌트입니다.
 *
 * [책임]
 * - Access Token 생성
 * - Refresh Token 생성
 * - 토큰 유효성 검증
 * - 토큰에서 인증 정보 추출
 *
 * [JWT 구조]
 * ```
 * Header.Payload.Signature
 * ```
 * - Header: 알고리즘 정보 (HS256)
 * - Payload: memberId, authorities, exp 등
 * - Signature: secret으로 서명
 *
 * [보안]
 * - HMAC SHA-256 알고리즘 사용
 * - Secret Key는 최소 256비트
 */
@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    private val secretKey = Keys.hmacShaKeyFor(
        jwtProperties.secret.toByteArray(StandardCharsets.UTF_8)
    )

    /**
     * Access Token을 생성합니다.
     *
     * @param memberId 회원 고유 ID
     * @param authorities 권한 목록 (예: ROLE_USER, ROLE_ADMIN)
     * @return JWT Access Token 문자열
     *
     * [Payload]
     * - sub: memberId (String)
     * - auth: authorities (comma-separated)
     * - iat: 발급 시각
     * - exp: 만료 시각
     */
    fun generateAccessToken(memberId: Long, authorities: Collection<GrantedAuthority>): String {
        val now = Date()
        val validity = Date(now.time + jwtProperties.accessTokenValidity)

        val authoritiesString = authorities.joinToString(",") { it.authority }

        return Jwts.builder()
            .setSubject(memberId.toString())
            .claim("auth", authoritiesString)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
            .also {
                logger.debug { "Generated access token for member: $memberId, authorities: $authoritiesString" }
            }
    }

    /**
     * Refresh Token을 생성합니다.
     *
     * @param memberId 회원 고유 ID
     * @return JWT Refresh Token 문자열
     *
     * [특징]
     * - Access Token보다 유효기간이 길다
     * - authorities 정보 미포함 (보안)
     * - Access Token 재발급에만 사용
     */
    fun generateRefreshToken(memberId: Long): String {
        val now = Date()
        val validity = Date(now.time + jwtProperties.refreshTokenValidity)

        return Jwts.builder()
            .setSubject(memberId.toString())
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
            .also {
                logger.debug { "Generated refresh token for member: $memberId" }
            }
    }

    /**
     * 토큰의 유효성을 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     *
     * [검증 항목]
     * - 서명 유효성 (secret key 일치)
     * - 만료 시각
     * - 토큰 형식
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: SecurityException) {
            logger.warn { "Invalid JWT signature: ${e.message}" }
            false
        } catch (e: MalformedJwtException) {
            logger.warn { "Invalid JWT token: ${e.message}" }
            false
        } catch (e: ExpiredJwtException) {
            logger.warn { "Expired JWT token: ${e.message}" }
            false
        } catch (e: UnsupportedJwtException) {
            logger.warn { "Unsupported JWT token: ${e.message}" }
            false
        } catch (e: IllegalArgumentException) {
            logger.warn { "JWT claims string is empty: ${e.message}" }
            false
        }
    }

    /**
     * 토큰에서 Authentication 객체를 추출합니다.
     *
     * @param token JWT 토큰
     * @return Spring Security Authentication 객체
     *
     * [처리 흐름]
     * 1. 토큰에서 Claims 추출
     * 2. memberId (subject) 추출
     * 3. authorities 추출 및 파싱
     * 4. UsernamePasswordAuthenticationToken 생성
     */
    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val memberId = claims.subject.toLong()
        val authoritiesString = claims["auth"] as? String ?: ""

        val authorities = if (authoritiesString.isNotEmpty()) {
            authoritiesString.split(",")
                .map { SimpleGrantedAuthority(it.trim()) }
        } else {
            emptyList()
        }

        logger.debug { "Extracted authentication for member: $memberId, authorities: $authoritiesString" }

        // principal: memberId, credentials: token, authorities: 권한 목록
        return UsernamePasswordAuthenticationToken(memberId, token, authorities)
    }

    /**
     * 토큰에서 Member ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return Member ID (Long)
     */
    fun getMemberId(token: String): Long {
        return parseClaims(token).subject.toLong()
    }

    /**
     * 토큰에서 Claims를 파싱합니다.
     *
     * @param token JWT 토큰
     * @return JWT Claims
     *
     * [주의]
     * - 만료된 토큰도 Claims 추출 가능 (ExpiredJwtException에서 추출)
     * - validateToken()과 함께 사용 권장
     */
    private fun parseClaims(token: String): Claims {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: ExpiredJwtException) {
            // 만료된 토큰도 Claims는 읽을 수 있음
            e.claims
        }
    }
}
