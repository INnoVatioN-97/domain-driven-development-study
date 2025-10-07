package com.innovation.dddexample.infrastructure.persistence.team

import com.innovation.dddexample.domain.team.model.Team
import org.springframework.data.jpa.repository.JpaRepository

interface TeamJpaRepository : JpaRepository<Team, Long> {
}