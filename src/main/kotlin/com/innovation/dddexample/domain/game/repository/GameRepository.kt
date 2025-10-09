package com.innovation.dddexample.domain.game.repository

import com.innovation.dddexample.domain.game.model.Game
import com.innovation.dddexample.interfaces.dto.game.ListWeeklyGamesResponse

interface GameRepository {
    fun save(game: Game): Game
    fun saveAll(games: List<Game>): List<Game>
    fun findById(id: Long): Game?
    fun findAll(): List<Game>
    fun findByDateRange(startDate: String, endDate: String): List<ListWeeklyGamesResponse>
    fun deleteById(id: Long)
    fun existsById(id: Long): Boolean
}
