package com.innovation.dddexample.infrastructure.persistence.game

import com.innovation.dddexample.domain.game.model.SeatGrade
import com.innovation.dddexample.domain.game.repository.SeatGradeRepository
import org.springframework.stereotype.Repository

@Repository
class SeatGradeRepositoryImpl(private val seatGradeJpaRepository: SeatGradeJpaRepository) : SeatGradeRepository {
    override fun saveAll(seatGrades: List<SeatGrade>): List<SeatGrade> {
        return seatGradeJpaRepository.saveAll(seatGrades)
    }

    override fun findByGameId(gameId: Long): List<SeatGrade> {
        return seatGradeJpaRepository.findByGameId(gameId)
    }
}