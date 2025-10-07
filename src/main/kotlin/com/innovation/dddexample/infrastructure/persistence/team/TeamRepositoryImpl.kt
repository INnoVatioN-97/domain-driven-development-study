package com.innovation.dddexample.infrastructure.persistence.team

import com.innovation.dddexample.domain.team.model.Team
import com.innovation.dddexample.domain.team.repository.TeamRepository
import org.springframework.stereotype.Repository

@Repository
class TeamRepositoryImpl(private val teamJpaRepository: TeamJpaRepository) : TeamRepository {
    override fun save(team: Team): Team {
        return teamJpaRepository.save(team)
    }

    override fun saveAll(team: List<Team>): List<Team> {
        return teamJpaRepository.saveAll(team)
    }

    override fun findById(id: Long): Team? {
        return teamJpaRepository.findById(id).orElse(null)
    }
}