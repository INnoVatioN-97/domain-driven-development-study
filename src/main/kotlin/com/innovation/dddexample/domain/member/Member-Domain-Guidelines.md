# Member Domain Reference

## Aggregate Snapshot
- Aggregate root `Member` 관리: 가입 시각(`registeredAt`), 탈퇴 시각(`withdrawnAt`), 상태 전이 메서드(`updateProfile`, `withdraw`, `changeEmail`)로 불변 조건을 강제합니다.
- Value Object `Email`, `PhoneNumber`는 각각 형식 검증과 정규화를 init 블록에서 수행하며, Member는 유효한 객체만을 통해 상태를 갱신합니다.
- 탈퇴 상태는 `isActive` / `isWithdrawn` 헬퍼로 노출되어 애플리케이션 계층이 도메인 용어로 상태를 확인하도록 합니다.

## Repository Capability Requirements (10/05 기준)
- **식별 조회**: ID 기반 단건 조회, 존재 여부 확인.
- **이메일 조회**: `Email` Value Object로 조회 및 중복 검증, 인증/로그인 플로우 대응.
- **상태 필터링**: 탈퇴 여부(`withdrawnAt`)를 이용한 활성 회원 전용 조회.
- **저장/수정**: Aggregate 단위 저장을 전제하며, 이메일 유니크 제약 위반 시 도메인 예외 매핑 전략 필요.
- **동시성**: JPA 낙관적 락 도입 여부 검토(`@Version`), 혹은 DB 제약 기반 재시도 정책 정의.

## Functional Spec Alignment
- 가입 플로우: 이메일 인증 성공 후 이름·휴대폰 필수 입력, 포인트 지갑 초기화/출석체크 확장 여지를 고려합니다.
- 고객센터/관리 시나리오: 이메일·예약 이력 조회 요구로 인해 조회 전용 포트(`MemberQueryRepository`) 및 MyBatis 확장을 대비합니다.
- 도메인 이벤트: 탈퇴, 이메일 변경 등에서 다른 Bounded Context로의 알림 필요성 검토.

## Testing Checklist
- 유효한 Email/PhoneNumber로 생성 및 `updateProfile` 성공.
- 잘못된 이메일/전화번호 생성 시 예외.
- 탈퇴 후 재탈퇴 금지 및 상태 전환 확인.
- `changeEmail` 동일 이메일 입력 시 예외.
- 이메일 중복 저장 시 예외 매핑 및 활성 회원 필터 쿼리 검증.

## Open Questions
- 포인트 지갑/출석체크 엔티티 모델링 시 Member Aggregate와의 경계 정의.
- 탈퇴 회원 재가입 정책(이메일 재사용 가능 여부).
- 조회용 MyBatis 매퍼 네이밍 및 DTO 구조.
