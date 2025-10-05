package com.innovation.dddexample.infrastructure.persistence.member

import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.model.Member
import com.innovation.dddexample.domain.member.repository.MemberRepository
import org.springframework.stereotype.Repository

/**
 * 도메인 계층에 정의된 MemberRepository 인터페이스의 구현 클래스입니다.
 * 인프라스트럭처 계층에 속하며, 실제 데이터 영속성 처리를 담당합니다.
 * @Repository 어노테이션을 통해 Spring 컨테이너가 이 클래스를 리포지토리 컴포넌트로 관리하게 합니다.
 */
@Repository
class MemberRepositoryImpl(
    // Spring이 자동으로 생성한 MemberJpaRepository 구현체를 여기에 주입(DI)합니다.
    private val memberJpaRepository: MemberJpaRepository
) : MemberRepository { // 도메인 리포지토리 인터페이스 구현

    /**
     * 실제 저장은 jpaRepository에 위임합니다.
     */
    override fun save(member: Member): Member {
        return memberJpaRepository.save(member)
    }

    /**
     * ID로 회원을 찾는 동작을 jpaRepository에 위임합니다.
     * findById는 Optional을 반환하므로, 결과가 없으면 null을 반환하도록 처리합니다.
     */
    override fun findById(id: Long): Member? {
        return memberJpaRepository.findById(id).orElse(null)
    }

    /**
     * 이메일로 회원을 찾는 동작을 jpaRepository에 위임합니다.
     */
    override fun findByEmail(email: Email): Member? {
        return memberJpaRepository.findByEmail(email)
    }

    /**
     * ID로 회원을 삭제하는 동작을 jpaRepository에 위임합니다.
     */
    override fun deleteById(id: Long) {
        memberJpaRepository.deleteById(id)
    }
}
