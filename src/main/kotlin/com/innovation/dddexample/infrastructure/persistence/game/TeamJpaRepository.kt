package com.innovation.dddexample.infrastructure.persistence.game

import com.innovation.dddexample.domain.game.model.Team
import org.springframework.data.jpa.repository.JpaRepository

interface TeamJpaRepository : JpaRepository<Team, Long> {
}
