package com.innovation.dddexample.infrastructure.seed.game

import com.innovation.dddexample.domain.game.model.Game
import com.innovation.dddexample.domain.game.repository.GameRepository
import com.innovation.dddexample.domain.team.model.Team
import com.innovation.dddexample.domain.team.repository.TeamRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class GameSeeder(
    private val gameRepository: GameRepository,
    private val teamRepository: TeamRepository
) {

    @Transactional
    fun seedGames() {

        val teams = teamRepository.findAll()
        if (teams.size < 2) {
            logger.warn { "Cannot generate games, not enough teams found." }
            return
        }

        logger.info { "Generating game schedules..." }

        val games = generateRoundRobinSchedule(teams)
        gameRepository.saveAll(games)

        logger.info { "Finished seeding ${games.size} games." }
    }

    private fun generateRoundRobinSchedule(teams: List<Team>): List<Game> {
        val games = mutableListOf<Game>()
        val totalMatchups = 16 // 구단 간 16차전

        var gameDate = LocalDate.of(2025, 4, 1) // 시즌 시작일

        repeat(totalMatchups) { matchupIndex ->
            val mutableTeams = teams.toMutableList()
            val isHomeGameSwap = matchupIndex >= totalMatchups / 2 // 9~16차전은 홈/어웨이 스왑

            val totalRounds = teams.size - 1
            repeat(totalRounds) {
                // 한 라운드의 경기들 생성 (5경기)
                for (i in 0 until teams.size / 2) {
                    val team1 = mutableTeams[i]
                    val team2 = mutableTeams[teams.size - 1 - i]

                    val homeTeam = if (isHomeGameSwap) team2 else team1
                    val awayTeam = if (isHomeGameSwap) team1 else team2

                    // 월요일은 경기 없음
                    while (gameDate.dayOfWeek == java.time.DayOfWeek.MONDAY) {
                        gameDate = gameDate.plusDays(1)
                    }
                    val gameTime = gameDate.atTime(18, 30)
                    games.add(Game(homeTeam = homeTeam, awayTeam = awayTeam, gameTime = gameTime))
                }
                gameDate = gameDate.plusDays(1) // 다음 5경기를 위해 날짜 하루 증가

                // 다음 라운드를 위해 팀 목록 회전 (첫 번째 팀은 고정)
                val lastTeam = mutableTeams.removeLast()
                mutableTeams.add(1, lastTeam)
            }
        }

        return games
    }
}
