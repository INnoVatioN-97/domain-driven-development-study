package com.innovation.dddexample.infrastructure.persistence.game

import com.innovation.dddexample.domain.game.model.Game
import com.innovation.dddexample.domain.game.repository.GameRepository
import com.innovation.dddexample.infrastructure.mybatis.game.GameMapper
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class GameRepositoryImpl(private val gameJpaRepository: GameJpaRepository, private val gameMybatisMapper: GameMapper) :
    GameRepository {

    override fun save(game: Game): Game {
        return gameJpaRepository.save(game)
    }

    override fun saveAll(games: List<Game>): List<Game> {
        return gameJpaRepository.saveAll(games)
    }

    override fun findById(id: Long): Game? {
        return gameJpaRepository.findById(id).orElse(null)
    }

    override fun findAll(): List<Game> {
        return gameJpaRepository.findAll()
    }

    override fun findByDateRange(
        startDate: LocalDateTime, endDate: LocalDateTime
    ): List<Game> {
        return gameJpaRepository.findByDateRange(startDate, endDate)
    }

    override fun deleteById(id: Long) {
        gameJpaRepository.deleteById(id)
    }

    override fun existsById(id: Long): Boolean {
        return gameJpaRepository.existsById(id)
    }
}