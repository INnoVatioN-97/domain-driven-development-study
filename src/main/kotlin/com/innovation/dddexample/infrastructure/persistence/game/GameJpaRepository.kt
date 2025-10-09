package com.innovation.dddexample.infrastructure.persistence.game

import com.innovation.dddexample.domain.game.model.Game
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface GameJpaRepository : JpaRepository<Game, Long> {

    @Query("SELECT g FROM Game g WHERE g.gameTime BETWEEN ?1 AND ?2")
    fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Game>
}