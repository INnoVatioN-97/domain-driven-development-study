package com.innovation.dddexample.infrastructure.seed

import com.innovation.dddexample.infrastructure.seed.game.GameSeeder
import com.innovation.dddexample.infrastructure.seed.team.TeamSeeder
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors


import org.springframework.context.annotation.Profile

@Profile("local", "dev")
@Component
class DataSeeder(
    private val teamSeeder: TeamSeeder,
    private val gameSeeder: GameSeeder
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        teamSeeder.seedTeams()
        gameSeeder.seedGames()
    }
}