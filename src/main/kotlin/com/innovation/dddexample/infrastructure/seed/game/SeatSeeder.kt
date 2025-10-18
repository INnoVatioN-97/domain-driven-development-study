package com.innovation.dddexample.infrastructure.seed.game

import com.innovation.dddexample.domain.game.model.Game
import com.innovation.dddexample.domain.game.model.Seat
import com.innovation.dddexample.domain.game.model.SeatGrade
import com.innovation.dddexample.domain.game.repository.GameRepository
import com.innovation.dddexample.domain.game.repository.SeatGradeRepository
import com.innovation.dddexample.domain.game.repository.SeatRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

@Service
class SeatSeeder(
    private val gameRepository: GameRepository,
    private val seatGradeRepository: SeatGradeRepository,
    private val seatRepository: SeatRepository
) {

    @Transactional
    fun seedSeats() {
        val games = gameRepository.findAll()
        if (games.isEmpty()) {
            logger.warn { "No games found, skipping seat seeding." }
            return
        }

        logger.info { "Seeding seats for ${games.size} games..." }

        games.forEach { game ->
            // 1. Create SeatGrades for the game
            val grades = createGradesForGame(game)
            val savedGrades = seatGradeRepository.saveAll(grades)

            // 2. Create Seats for the game
            val seats = createSeatsForGame(game, savedGrades)
            seatRepository.saveAll(seats)
        }

        logger.info { "Finished seeding seats." }
    }

    private fun createGradesForGame(game: Game): List<SeatGrade> {
        return listOf(
            SeatGrade(game = game, name = "VIP", price = BigDecimal("30000")),
            SeatGrade(game = game, name = "R", price = BigDecimal("25000")),
            SeatGrade(game = game, name = "S", price = BigDecimal("20000"))
        )
    }

    private fun createSeatsForGame(game: Game, grades: List<SeatGrade>): List<Seat> {
        val seats = mutableListOf<Seat>()
        val vipGrade = grades.first { it.name == "VIP" }
        val rGrade = grades.first { it.name == "R" }
        val sGrade = grades.first { it.name == "S" }

        for (i in 1..100) {
            val seatNumber = "A${i.toString().padStart(3, '0')}"
            val grade = when {
                i <= 20 -> vipGrade // 1-20 are VIP
                i <= 50 -> rGrade   // 21-50 are R
                else -> sGrade      // 51-100 are S
            }
            seats.add(Seat(game = game, seatGrade = grade, seatNumber = seatNumber))
        }
        return seats
    }
}
