package com.innovation.dddexample.domain.game.repository

import com.innovation.dddexample.domain.game.model.SeatGrade

interface SeatGradeRepository {
    fun saveAll(seatGrades: List<SeatGrade>): List<SeatGrade>

    fun findByGameId(gameId: Long): List<SeatGrade>
}
