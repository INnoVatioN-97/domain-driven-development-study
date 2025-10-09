package com.innovation.dddexample.infrastructure.persistence.reservation

import com.innovation.dddexample.domain.reservation.model.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReservationJpaRepository: JpaRepository<Reservation, Long> {
    @Query("SELECT r FROM Reservation r WHERE r.memberId = :memberId AND r.gameId IN :gameIds")
    fun findByMemberIdAndGameIdIn(memberId: Long, gameIds: List<Long>): List<Reservation>
}