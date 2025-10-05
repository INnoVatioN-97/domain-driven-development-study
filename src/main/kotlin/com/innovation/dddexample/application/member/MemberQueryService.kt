package com.innovation.dddexample.application.member

import com.innovation.dddexample.domain.member.exception.MemberNotFoundException
import com.innovation.dddexample.domain.member.model.Member
import com.innovation.dddexample.domain.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회원 조회 유스케이스를 담당하는 Application Service입니다.
 *
 * [DDD 패턴: Application Service]
 * - 도메인 객체들을 조율(orchestrate)하여 유스케이스를 수행
 * - 도메인 로직은 포함하지 않음 (도메인 객체에 위임)
 * - 트랜잭션 경계 설정
 * - 여러 인터페이스(REST, GraphQL, CLI)에서 재사용 가능
 *
 * [Application Service vs Domain Service]
 * - Application Service: 유스케이스 조율, 트랜잭션 관리, 여러 도메인 객체 협업
 * - Domain Service: 여러 Aggregate를 넘나드는 도메인 로직 (단일 Aggregate로 해결 안되는 경우)
 *
 * [왜 Controller에서 Repository를 직접 호출하지 않나?]
 * 1. 재사용성: 다른 인터페이스(GraphQL, CLI 등)에서도 같은 로직 사용 가능
 * 2. 테스트 용이성: HTTP 없이 비즈니스 로직 테스트 가능
 * 3. 트랜잭션 경계: 서비스 메서드가 자연스러운 트랜잭션 단위
 * 4. 확장성: 나중에 캐싱, 로깅, 이벤트 발행 등 추가 가능
 */
@Service
@Transactional(readOnly = true)  // 읽기 전용 트랜잭션: DB 최적화 힌트
class MemberQueryService(
    private val memberRepository: MemberRepository
) {
    /**
     * ID로 회원을 조회합니다.
     *
     * @param id 조회할 회원의 고유 ID
     * @return 찾은 Member 엔티티
     * @throws MemberNotFoundException 회원을 찾을 수 없는 경우
     *
     * [Elvis Operator 활용]
     * - repository.findById(id) ?: throw ... 는 Kotlin 관용구
     * - null이면 예외 발생, 값이 있으면 반환
     * - Java보다 간결하면서도 명확함
     */
    fun getMemberById(id: Long): Member {
        return memberRepository.findById(id)
            ?: throw MemberNotFoundException.byId(id)
    }
}
