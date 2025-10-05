package com.innovation.dddexample.domain.member.service

import com.innovation.dddexample.domain.member.exception.DuplicateEmailException
import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.repository.MemberRepository
import org.springframework.stereotype.Component

/**
 * Member와 관련된 도메인 규칙을 검증하는 서비스입니다.
 * 여러 Aggregate가 필요하거나, Repository 조회가 필요한 도메인 로직을 처리합니다.
 */
@Component
class MemberDomainService(
    private val memberRepository: MemberRepository
) {

    /**
     * 이메일 주소가 이미 사용 중인지 검증합니다.
     *
     * @throws DuplicateEmailException 이메일이 이미 존재할 경우
     */
    fun validateUniqueEmail(email: Email) {
        if (memberRepository.findByEmail(email) != null) {
            throw DuplicateEmailException(email.value)
        }
    }
}
