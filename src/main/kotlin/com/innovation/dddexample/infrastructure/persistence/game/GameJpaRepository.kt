package com.innovation.dddexample.infrastructure.persistence.game

import com.innovation.dddexample.domain.game.model.Game
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface GameJpaRepository : JpaRepository<Game, Long> {
    fun findByGameTimeIsGreaterThanEqualAndGameTimeIsLessThanEqual(
        gameTimeIsGreaterThan: LocalDateTime,
        gameTimeIsLessThan: LocalDateTime
    ): MutableList<Game>
}