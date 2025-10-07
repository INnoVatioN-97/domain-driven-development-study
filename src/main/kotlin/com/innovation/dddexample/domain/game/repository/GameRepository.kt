package com.innovation.dddexample.domain.game.repository

import com.innovation.dddexample.domain.game.model.Game
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface GameRepository{
    fun save(game: Game): Game
    fun saveAll(games: List<Game>): List<Game>
    fun findById(id: Long): Game?
    fun findAll(): List<Game>
    fun deleteById(id: Long)
    fun existsById(id: Long): Boolean
}
