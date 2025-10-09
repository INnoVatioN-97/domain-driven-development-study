package com.innovation.dddexample.infrastructure.persistence.reservation

import com.innovation.dddexample.domain.reservation.model.Reservation
import org.springframework.data.jpa.repository.JpaRepository

interface ReservationJpaRepository: JpaRepository<Reservation, Long> {
}