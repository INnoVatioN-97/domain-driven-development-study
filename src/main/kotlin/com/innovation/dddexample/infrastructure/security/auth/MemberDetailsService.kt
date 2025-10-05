package com.innovation.dddexample.infrastructure.security.auth

import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.repository.MemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * Spring Security의 UserDetailsService를 구현한 서비스입니다.
 *
 * [역할]
 * - 사용자 이름(이메일)으로 회원 정보 조회
 * - Member 엔티티 → MemberDetails 변환
 * - Spring Security 인증 프로세스에서 사용
 *
 * [사용 시나리오]
 * 1. 로그인 요청 (향후 구현 시)
 *    - 이메일로 회원 조회
 *    - 비밀번호 검증 (PasswordEncoder)
 *    - JWT 토큰 발급
 *
 * 2. JWT 기반 인증 (현재)
 *    - JwtTokenProvider가 토큰 검증
 *    - 토큰에서 memberId 추출
 *    - SecurityContext에 인증 정보 설정
 *    - UserDetailsService는 직접 사용 안함 (JWT가 대체)
 *
 * [참고]
 * - JWT 기반 시스템에서는 매 요청마다 DB 조회 불필요
 * - 로그인 시에만 사용 (토큰 발급 시)
 */
@Service
class MemberDetailsService(
    private val memberRepository: MemberRepository
) : UserDetailsService {

    /**
     * 이메일로 회원 정보를 조회합니다.
     *
     * @param username 이메일 주소 (UserDetails의 username)
     * @return MemberDetails (UserDetails 구현체)
     * @throws UsernameNotFoundException 회원을 찾을 수 없는 경우
     *
     * [처리 흐름]
     * 1. 이메일로 Member 조회
     * 2. Member → MemberDetails 변환
     * 3. Spring Security가 비밀번호 검증 (향후 로그인 기능 추가 시)
     */
    override fun loadUserByUsername(username: String): UserDetails {
        logger.debug { "Loading user by username (email): $username" }

        val email = Email(username)
        val member = memberRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found with email: $username")
                .also { logger.warn { "Member not found: $username" } }

        logger.info { "User loaded successfully: ${member.email.value}" }
        return MemberDetails(member)
    }

    /**
     * Member ID로 회원 정보를 조회합니다.
     *
     * [사용처]
     * - JWT 토큰에서 memberId 추출 후 회원 정보 조회
     * - 필요 시 현재 인증된 회원의 최신 정보 갱신
     *
     * @param memberId 회원 고유 ID
     * @return MemberDetails
     * @throws UsernameNotFoundException 회원을 찾을 수 없는 경우
     *
     * [참고]
     * - JWT 기반 시스템에서는 매 요청마다 호출 불필요
     * - 필요 시에만 호출 (예: 권한 변경 확인)
     */
    fun loadUserById(memberId: Long): MemberDetails {
        logger.debug { "Loading user by ID: $memberId" }

        val member = memberRepository.findById(memberId)
            ?: throw UsernameNotFoundException("User not found with id: $memberId")
                .also { logger.warn { "Member not found: $memberId" } }

        logger.info { "User loaded successfully: ${member.id}" }
        return MemberDetails(member)
    }
}
