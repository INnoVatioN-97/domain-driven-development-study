package com.innovation.dddexample.application.member

import com.innovation.dddexample.application.common.UseCase
import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.model.Member
import com.innovation.dddexample.domain.member.model.PhoneNumber
import com.innovation.dddexample.domain.member.repository.MemberRepository
import com.innovation.dddexample.domain.member.service.MemberDomainService
import com.innovation.dddexample.infrastructure.security.jwt.JwtTokenProvider
import com.innovation.dddexample.interfaces.dto.auth.TokenResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SignUpMemberUseCase(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberDomainService: MemberDomainService
) : UseCase<SignUpMemberCommand, TokenResponse> {

    override fun execute(command: SignUpMemberCommand): TokenResponse {
        val email = Email(command.email)
        memberDomainService.validateUniqueEmail(email)

        val member: Member = memberRepository.save(
            Member(
                email = email,
                name = command.name,
                phoneNumber = PhoneNumber(command.phoneNumber),
                password = passwordEncoder.encode(command.password)
            )
        )


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