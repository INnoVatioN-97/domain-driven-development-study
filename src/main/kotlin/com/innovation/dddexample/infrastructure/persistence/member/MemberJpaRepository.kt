package com.innovation.dddexample.infrastructure.persistence.member

import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.model.Member
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA 리포지토리 인터페이스.
 * JpaRepository<Member, Long>를 상속받으면 기본적인 CRUD 메서드(save, findById, delete 등)가 자동으로 제공됩니다.
 * Spring이 실행 시점에 이 인터페이스의 구현체를 자동으로 생성해 줍니다.
 */
interface MemberJpaRepository : JpaRepository<Member, Long> {
    /**
     * Spring Data JPA의 'Query Method' 기능입니다.
     * 메서드 이름을 규칙에 맞게 지으면 (e.g., findBy[필드이름]), Spring이 자동으로 해당 쿼리를 생성합니다.
     * 여기서는 Member 엔티티의 'email' 필드를 기준으로 회원을 찾는 쿼리가 생성됩니다.
     */
    fun findByEmail(email: Email): Member?
}
