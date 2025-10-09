package com.innovation.dddexample.infrastructure.persistence.reservation

import com.innovation.dddexample.domain.reservation.model.Reservation
import com.innovation.dddexample.domain.reservation.repository.ReservationRepository
import org.springframework.stereotype.Repository

@Repository
class ReservationRepositoryImpl(private val reservationJpaRepository: ReservationJpaRepository) :
    ReservationRepository {
    override fun findById(id: Long): Reservation? {
        return reservationJpaRepository.findById(id).orElse(null)
    }

    override fun save(reservation: Reservation): Reservation {
        return reservationJpaRepository.save(reservation)
    }
}