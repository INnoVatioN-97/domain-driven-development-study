package com.innovation.dddexample.domain.game.repository

import com.innovation.dddexample.domain.game.model.Seat

interface SeatRepository : SeatRepositoryCustom {
    fun findById(id: Long): Seat?//

    fun findByGameId(gameId: Long): List<Seat>

    fun saveAll(seats: List<Seat>): List<Seat>
}