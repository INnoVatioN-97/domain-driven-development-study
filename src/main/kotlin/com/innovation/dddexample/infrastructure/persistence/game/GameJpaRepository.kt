package com.innovation.dddexample.infrastructure.persistence.game

import com.innovation.dddexample.domain.game.model.Game
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.Optional

interface GameJpaRepository : JpaRepository<Game, Long> {

    @Query("SELECT g FROM Game g JOIN FETCH g.homeTeam JOIN FETCH g.awayTeam WHERE g.gameTime BETWEEN :startDate AND :endDate ORDER BY g.gameTime ASC")
    fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Game>

    @Query("SELECT g FROM Game g JOIN FETCH g.homeTeam JOIN FETCH g.awayTeam WHERE g.id = :id")
    fun findGameDetailsById(id: Long): Optional<Game>
}