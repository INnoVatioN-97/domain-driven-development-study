package com.innovation.dddexample.infrastructure.seed


import com.innovation.dddexample.infrastructure.seed.game.GameSeeder
import com.innovation.dddexample.infrastructure.seed.game.SeatSeeder
import com.innovation.dddexample.infrastructure.seed.game.TeamSeeder
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("local", "dev")
@Component
class DataSeeder(
    private val teamSeeder: TeamSeeder,
    private val gameSeeder: GameSeeder,
    private val seatSeeder: SeatSeeder
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        teamSeeder.seedTeams()
        gameSeeder.seedGames()
        seatSeeder.seedSeats()
    }
}