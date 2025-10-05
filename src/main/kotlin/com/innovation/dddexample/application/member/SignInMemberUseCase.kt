package com.innovation.dddexample.application.member

import com.innovation.dddexample.application.common.UseCase
import com.innovation.dddexample.domain.member.exception.MemberNotFoundException
import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.infrastructure.security.jwt.JwtTokenProvider
import com.innovation.dddexample.domain.member.repository.MemberRepository
import com.innovation.dddexample.interfaces.dto.auth.TokenResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SignInMemberUseCase(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) : UseCase<SignInMemberCommand, TokenResponse> {

    override fun execute(command: SignInMemberCommand): TokenResponse {
        val member = memberRepository.findByEmail(Email(command.email))
            ?: throw MemberNotFoundException(0) // ID를 모르므로 임시로 0 전달, 예외 메시지 개선 필요

        if (!passwordEncoder.matches(command.password, member.password)) {
            throw IllegalArgumentException("Invalid password")
        }

        return TokenResponse(
            accessToken = jwtTokenProvider.generateAccessToken(
                memberId = member.id!!,
                authorities = listOf(SimpleGrantedAuthority("ROLE_${member.role.name}"))
            ),
            refreshToken = jwtTokenProvider.generateRefreshToken(
                memberId = member.id
            ),
        )
    }
}