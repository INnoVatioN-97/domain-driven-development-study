package com.innovation.dddexample.infrastructure.persistence.game

import com.innovation.dddexample.domain.game.model.QSeat
import com.innovation.dddexample.domain.game.model.QSeatGrade
import com.innovation.dddexample.domain.game.model.Seat
import com.innovation.dddexample.domain.game.model.SeatStatus
import com.innovation.dddexample.domain.game.repository.SeatRepository
import com.innovation.dddexample.domain.game.repository.SeatRepositoryCustom
import com.innovation.dddexample.interfaces.dto.game.SeatSummaryInfo
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class SeatRepositoryImpl(
    private val seatJPARepository: SeatJPARepository,
    private val queryFactory: JPAQueryFactory
) : SeatRepository, SeatRepositoryCustom {
    override fun findById(id: Long): Seat? {
        return seatJPARepository.findById(id).orElse(null)

    }

    override fun findByGameId(gameId: Long): List<Seat> {
        return seatJPARepository.findAllByGameId(gameId)
    }

    override fun saveAll(seats: List<Seat>): List<Seat> {
        return seatJPARepository.saveAll(seats)
    }

    override fun findSeatSummaryByGameId(gameId: Long): List<SeatSummaryInfo> {
        // 1. QueryDSL에서 사용할 Q-Type 인스턴스를 생성합니다.
        // 엔티티 메타데이터를 나타내며, 타입-세이프한 쿼리 작성을 가능하게 합니다.
        val seat = QSeat.seat
        val seatGrade = QSeatGrade.seatGrade

        // 2. SQL의 CASE WHEN 구문을 타입-세이프하게 작성합니다.
        // 좌석 상태(status)가 AVAILABLE이면 1, 아니면 0을 반환하여 합산(sum)함으로써 예매 가능한 좌석 수를 계산합니다.
        val availableSeats = Expressions.cases()
            .`when`(seat.status.eq(SeatStatus.AVAILABLE)).then(1L)
            .otherwise(0L)
            .sum()

        // 3. 실제 쿼리 실행
        return queryFactory
            // 4. SELECT 절: 조회할 대상을 지정합니다.
            .select(
                // Projections.constructor를 사용해 조회 결과를 DTO(SeatSummaryInfo)로 직접 매핑합니다.
                // SQL의 "SELECT sg.name, COUNT(s.id), SUM(CASE ...)" 구문과 유사합니다.
                Projections.constructor(
                    SeatSummaryInfo::class.java,
                    seatGrade.name, // 좌석 등급 이름
                    seat.count(),   // 해당 등급의 총 좌석 수
                    availableSeats  // 위에서 정의한 예매 가능 좌석 수
                )
            )
            // 5. FROM 절: 쿼리의 기본 대상을 지정합니다. (FROM seats)
            .from(seat)
            // 6. JOIN 절: 좌석(seat)과 좌석 등급(seatGrade)을 조인합니다. (JOIN seat_grades ON seat.seat_grade_id = seat_grade.id)
            .join(seat.seatGrade, seatGrade)
            // 7. WHERE 절: 특정 경기에 해당하는 좌석만 필터링합니다. (WHERE seat.game_id = :gameId)
            .where(seat.game.id.eq(gameId))
            // 8. GROUP BY 절: 좌석 등급 이름(seatGrade.name)으로 그룹화하여 등급별 집계를 수행합니다.
            .groupBy(seatGrade.name)
            // 9. 쿼리를 실행하고 결과를 리스트(List<SeatSummaryInfo>)로 반환받습니다.
            .orderBy(seatGrade.price.desc())
            // 10. 가격순으로 DESC 정렬합니다.
            .fetch()
    }
}