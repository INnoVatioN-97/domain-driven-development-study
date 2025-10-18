package com.innovation.dddexample.infrastructure.persistence.game

import com.innovation.dddexample.domain.game.model.Team
import com.innovation.dddexample.domain.game.repository.TeamRepository
import org.springframework.stereotype.Repository

@Repository
class TeamRepositoryImpl(private val teamJpaRepository: TeamJpaRepository) : TeamRepository {
    override fun save(team: Team): Team {
        return teamJpaRepository.save(team)
    }

    override fun saveAll(teams: List<Team>): List<Team> {
        return teamJpaRepository.saveAll(teams)
    }

    override fun findById(id: Long): Team? {
        return teamJpaRepository.findById(id).orElse(null)
    }

    override fun findAll(): List<Team> {
        return teamJpaRepository.findAll()
    }
}
