# Game 도메인 개발 일지 (2025-10-18)

## 1. 목표

- **기능 명세:** `UC-006: 경기 상세 정보 및 좌석 조회` 구현
- **핵심 과업:** 사용자가 특정 경기를 선택했을 때, 경기 상세 정보와 함께 좌석 등급, 가격, 잔여 좌석, 전체 좌석 배치도를 제공하는 API 개발

## 2. Aggregate 구조 설계

`Game` 애그리거트는 경기와 좌석 정보를 포함하는 경계를 가진다.

- **`Game` (Root):** 경기 정보를 나타내는 애그리거트 루트. 
- **`Seat` (Entity):** 개별 좌석. 좌석 번호, 등급, 예매 상태 등의 정보를 가짐. `Game`에 종속된다.
- **`SeatGrade` (Entity):** 좌석 등급. (예: VIP, R, S석). 등급명, 가격 등의 정보를 가짐. `Game`에 종속되며, 여러 `Seat`에서 공유될 수 있다.
Game
   │
   ├── Seat (1..N)
   │
   └── SeatGrade (1..N)

## 3. Repository 인터페이스 정의

- **`GameRepository` 확장:**
    - `findById(gameId: Long): Game?`: ID로 `Game`을 조회한다. `Seat`과 `SeatGrade` 정보가 함께 필요할 수 있으므로 `fetch join`을 고려한다.
- **`SeatRepository`:**
    - `findByGameId(gameId: Long): List<Seat>`: 특정 경기의 모든 좌석 정보를 조회한다.

## 4. UseCase 및 DTO 설계

- **`GetGameDetailsUseCase`:**
    - **Input:** `gameId: Long`
    - **Output:** `GameDetailsResponse` (DTO)
    - **로직:**
        1. `gameId`로 `GameRepository`를 통해 `Game`와 관련 `SeatGrade` 정보를 조회한다.
        2. `gameId`로 `SeatRepository`를 통해 모든 `Seat` 목록을 조회한다.
        3. 조회된 도메인 객체들을 조합하여 `GameDetailsResponse` DTO를 생성하여 반환한다.

- **`GameDetailsResponse` (DTO):**
    - `gameId: Long`
    - `gameTime: LocalDateTime`
    - `homeTeamName: String`
    - `awayTeamName: String`
    - `stadium: String`
    - `grades: List<SeatGradeInfo>`: 좌석 등급별 정보
        - `gradeName: String`
        - `price: BigDecimal`
        - `totalSeats: Int`
        - `remainingSeats: Int`
    - `seats: List<SeatInfo>`: 전체 좌석 배치도 정보
        - `seatId: Long`
        - `gradeName: String`
        - `seatNumber: String`
        - `status: String` (AVAILABLE, RESERVED, SELECTED)

## 5. 테스트 체크리스트

- [ ] `Game` 애그리거트(`Game`, `Seat`, `SeatGrade`) 모델링 및 구현
- [ ] `GameRepository` 인터페이스 및 구현체 (JPA) 작성
- [ ] `GetGameDetailsUseCase` 단위 테스트
    - [ ] 특정 `gameId`에 대해 올바른 DTO가 생성되는지 검증
    - [ ] 존재하지 않는 `gameId`에 대한 예외 처리 검증
- [ ] `GET /api/games/{gameId}` API 통합 테스트
    - [ ] 200 OK 응답 및 정확한 데이터 반환 검증
    - [ ] 404 Not Found 응답 검증

## 6. 열린 질문 및 이슈

1.  **`Game` vs `Performance`:** 현재 `Game` 엔티티가 `Performance` 애그리거트 루트의 역할을 하고 있다. 명확성을 위해 `Game`을 `Performance`로 리네이밍할 것인가? 아니면 `Game`을 그대로 두고 `Performance`라는 개념을 더 넓은 범위의 Bounded Context로 볼 것인가?
    - **결정:** 우선 `Game`을 그대로 사용하고, 추후 뮤지컬, 콘서트 등 다른 종류의 '공연'이 추가될 때 리팩토링을 고려한다.
2.  **`SeatGrade`를 Entity로 할 것인가, VO로 할 것인가?**
    - **결정:** `SeatGrade`는 가격 등 변경될 수 있는 속성을 가지며, 고유한 식별자(ID)를 통해 관리되는 것이 명확하므로 **Entity**로 모델링한다.
3.  **Seeder 확장:** `Seat` 및 `SeatGrade` 데이터를 생성하는 로직을 기존 `DataSeeder.kt`에 추가해야 한다. 구장(Stadium)별로 좌석 배치가 다르다는 점을 어떻게 반영할 것인가?
    - **결정:** 초기에는 모든 구장이 동일한 좌석 배치(예: 100석)를 가진다고 가정하고 시더를 구현한다.
