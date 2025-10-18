package com.innovation.dddexample.infrastructure.persistence.game

import com.innovation.dddexample.domain.game.model.Seat
import com.innovation.dddexample.domain.game.repository.SeatRepository
import org.springframework.stereotype.Repository

@Repository
class SeatRepositoryImpl(private val seatJPARepository: SeatJPARepository) : SeatRepository {
    override fun findById(id: Long): Seat? {
        return seatJPARepository.findById(id).orElse(null)

    }

    override fun findByGameId(gameId: Long): List<Seat> {
        return seatJPARepository.findAllByGameId(gameId)
    }

    override fun saveAll(seats: List<Seat>): List<Seat> {
        return seatJPARepository.saveAll(seats)
    }
}