package com.innovation.dddexample.infrastructure.security.auth

import com.innovation.dddexample.domain.member.model.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Spring Security의 UserDetails를 구현한 Member 인증 정보입니다.
 *
 * [역할]
 * - Spring Security가 인증/인가에 사용하는 사용자 정보
 * - Member 도메인 엔티티를 Spring Security가 이해할 수 있는 형태로 변환
 *
 * [DDD 관점]
 * - Infrastructure 계층에 위치 (기술 세부사항)
 * - Domain의 Member를 래핑
 * - Domain은 Spring Security를 모름 (의존성 역전)
 *
 * [주의]
 * - UserDetails는 Spring Security 인터페이스
 * - Member 엔티티에 UserDetails를 구현하지 말 것 (도메인 오염)
 */
class MemberDetails(
    private val member: Member
) : UserDetails {

    /**
     * 회원 고유 ID를 반환합니다.
     *
     * [사용처]
     * - JWT 토큰 생성 시 subject
     * - 인증 후 현재 사용자 식별
     */
    val memberId: Long
        get() = member.id ?: throw IllegalStateException("Member ID must not be null")

    /**
     * 권한 목록을 반환합니다.
     *
     * [현재 구현]
     * - 모든 회원은 ROLE_USER 권한 보유
     * - 향후 Member 엔티티에 역할(Role) 추가 시 수정
     *
     * [향후 확장]
     * - Member.roles: Set<Role> 추가
     * - ROLE_ADMIN, ROLE_MANAGER 등 추가
     */
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))

        // 향후 Member에 roles 추가 시:
        // return member.roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }
    }

    /**
     * 사용자 이름 (식별자)을 반환합니다.
     *
     * [구현]
     * - 이메일을 username으로 사용
     * - 로그인 시 이메일로 인증
     */
    override fun getUsername(): String {
        return member.email.value
    }

    /**
     * 비밀번호를 반환합니다.
     *
     * [현재 구현]
     * - Member 엔티티에 password 필드 없음
     * - 빈 문자열 반환 (JWT 기반 인증이므로 불필요)
     *
     * [향후 로그인 기능 추가 시]
     * - Member에 password 필드 추가
     * - BCryptPasswordEncoder로 암호화된 비밀번호 저장
     * - return member.password
     */
    override fun getPassword(): String {
        return ""  // JWT 기반 인증, 비밀번호 미사용

        // 향후 로그인 기능 추가 시:
        // return member.password
    }

    /**
     * 계정이 만료되지 않았는지 여부를 반환합니다.
     *
     * [구현]
     * - 탈퇴한 회원(withdrawnAt != null)은 만료된 것으로 간주
     * - true: 계정 유효, false: 계정 만료
     */
    override fun isAccountNonExpired(): Boolean {
        return !member.isWithdrawn()
    }

    /**
     * 계정이 잠기지 않았는지 여부를 반환합니다.
     *
     * [현재 구현]
     * - 계정 잠금 기능 없음 (항상 true)
     *
     * [향후 확장]
     * - Member에 lockedAt 필드 추가
     * - 로그인 실패 5회 시 계정 잠금 등
     */
    override fun isAccountNonLocked(): Boolean {
        return true

        // 향후 계정 잠금 기능 추가 시:
        // return member.lockedAt == null
    }

    /**
     * 자격 증명(비밀번호)이 만료되지 않았는지 여부를 반환합니다.
     *
     * [현재 구현]
     * - 비밀번호 만료 기능 없음 (항상 true)
     *
     * [향후 확장]
     * - 비밀번호 90일마다 변경 강제 등
     */
    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    /**
     * 계정이 활성화되었는지 여부를 반환합니다.
     *
     * [구현]
     * - 탈퇴하지 않은 회원만 활성화
     * - isAccountNonExpired()와 동일한 로직
     */
    override fun isEnabled(): Boolean {
        return !member.isWithdrawn()
    }

    /**
     * Member 도메인 엔티티를 반환합니다.
     *
     * [사용처]
     * - 인증 후 현재 회원 정보 조회
     * - 비즈니스 로직에서 Member 필요 시
     */
    fun getMember(): Member {
        return member
    }
}
