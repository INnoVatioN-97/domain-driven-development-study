package com.innovation.dddexample.infrastructure.seed

import com.innovation.dddexample.infrastructure.seed.team.TeamSeeder
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors


@Component
class DataSeeder(
    private val teamSeeder: TeamSeeder,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        teamSeeder.seedTeams()
    }
}