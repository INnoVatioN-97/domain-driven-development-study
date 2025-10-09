package com.innovation.dddexample.domain.reservation.repository

import com.innovation.dddexample.domain.reservation.model.Reservation

/**
 * 예매 애그리거트의 Repository 인터페이스
 */
interface ReservationRepository {
    fun findById(id: Long): Reservation?

    fun save(reservation: Reservation): Reservation
}
