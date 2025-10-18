package com.innovation.dddexample.infrastructure.persistence.game

import com.innovation.dddexample.domain.game.model.Seat
import org.springframework.data.jpa.repository.JpaRepository

interface SeatJPARepository : JpaRepository<Seat, Long> {

    fun findAllByGameId(gameId:Long): List<Seat>
}