# KBO 티켓 예매 서비스 개발 로드맵

> 이 문서는 프로젝트의 공식 개발 로드맵입니다. 기존의 날짜 기반 마일스톤을 대체하며, 기능 중심의 WBS(Work Breakdown Structure) 형식으로 관리됩니다.

---

### 🎯 단기 최우선 목표 (Next Steps)

**JMeter를 이용한 동시성 테스트 환경 구축**을 위해 아래 기능의 MVP(Minimum Viable Product)를 우선적으로 개발합니다.

- **예매 생성 (R-01)**: 결제 로직은 Mocking/제외하더라도, 특정 좌석을 선택하여 예매를 생성하는 핵심 흐름을 완성합니다.
- **좌석 잠금 (R-02)**: 예매 생성 과정에서 발생하는 동시성 문제를 제어하기 위한 좌석 잠금 메커니즘을 구현합니다.

---

## 📝 WBS (Work Breakdown Structure)

### 1. 회원 (Member) Bounded Context

| 기능 ID | 기능 명세 | 상태 | 비고 |
| :--- | :--- | :--- | :--- |
| M-01 | 회원 가입 (이메일 인증) | ✅ 완료 | `Member` 애그리거트, `Email`/`PhoneNumber` VO 구현 완료 |
| M-02 | 로그인 (인증) | ✅ 완료 | Spring Security, JWT 기반 인증 구현 완료 |
| M-03 | 계정 관리 (휴면/탈퇴) | 📋 예정 | `DormancyPolicy` 도메인 서비스 설계 필요 |
| M-04 | 포인트 지갑 | 📋 예정 | `MemberPointWallet`, `PointTransaction` 엔티티 설계 필요 |
| M-05 | 출석 체크 | 📋 예정 | `AttendanceRecord` 엔티티 설계 필요 |

### 2. 공연 관리 (Performance Management) Bounded Context

| 기능 ID | 기능 명세 | 상태 | 비고 |
| :--- | :--- | :--- | :--- |
| P-01 | 경기/팀 정보 모델링 | ✅ 완료 | `Game`, `Team` 엔티티 구현 완료 |
| P-02 | 좌석/등급 정보 모델링 | ✅ 완료 | `Seat`, `SeatGrade` 엔티티 구현 완료 |
| P-03 | 주간 경기 목록 조회 API | ✅ 완료 | `GET /games/weekly` |
| P-04 | 예매 가능 좌석 조회 API | ✅ 완료 | `GET /games/seat` |
| P-05 | 경기 상세 정보 조회 API | ✅ 완료 | `GET /games/{gameId}` (QueryDSL 리팩토링 포함) |
| P-06 | 데이터 시딩 | ✅ 완료 | `TeamSeeder`, `GameSeeder`, `SeatSeeder` 구현 완료 |
| P-07 | 좌석 템플릿 관리 | 📋 예정 | 관리자 기능. CSV/JSON 업로드 기능 필요 |
| P-08 | 프로모션 코드 관리 | 📋 예정 | `PromotionCampaign` 애그리거트 설계 필요 |

### 3. 예매 (Reservation) Bounded Context

| 기능 ID | 기능 명세 | 상태 | 비고 |
| :--- | :--- | :--- | :--- |
| R-01 | 예매 생성 | 🚧 **진행 중 (최우선)** | 결제 제외 MVP 우선 개발 |
| R-02 | 좌석 잠금 (동시성 제어) | 🚧 **진행 중 (최우선)** | `SeatHold` 애그리거트, Optimistic/Pessimistic Lock 전략 필요 |
| R-03 | 결제 처리 | 📋 예정 | `Payment` 애그리거트, 포인트 사용/PG 연동 추상화 |
| R-04 | 예매 취소/환불 | 📋 예정 | `CancellationPolicy` VO, `Refund` 도메인 서비스 설계 필요 |
| R-05 | 대기열 시스템 | 📋 예정 | `QueueTicket` 애그리거트, 선착순 정책 |
| R-06 | 내 예매 내역 조회 | 📋 예정 | 조회 전용 쿼리 (MyBatis/QueryDSL) 필요 |

### 4. 관리자/고객센터 기능

| 기능 ID | 기능 명세 | 상태 | 비고 |
| :--- | :--- | :--- | :--- |
| A-01 | 예매 현황 대시보드 | 📋 예정 | |
| A-02 | 통계 보고서 (CSV/Excel) | 📋 예정 | |
| A-03 | 예매 강제 취소/포인트 정정 | 📋 예정 | |
| A-04 | 감사 로그 | 📋 예정 | |
