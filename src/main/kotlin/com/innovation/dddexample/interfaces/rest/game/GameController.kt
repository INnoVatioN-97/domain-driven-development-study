package com.innovation.dddexample.interfaces.rest.game

import com.innovation.dddexample.application.game.*
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/games")
class GameController(
    private val listWeeklyGamesUseCase: ListWeeklyGamesUseCase,
    private val listAvailableSeatListUseCase: ListAvailableSeatListUseCase,
    private val getGameDetailUseCase: GetGameDetailUseCase
) {
    @GetMapping("/weekly")
    fun listWeeklyGames(@RequestParam("date") date: String) =
        listWeeklyGamesUseCase.execute(ListWeeklyGamesCommand(date))

    @GetMapping("/seat")
    fun listAvailableSeats(@RequestParam("gameId") gameId: Long) =
        listAvailableSeatListUseCase.execute(ListAvailableSeatListCommand(gameId))

    @GetMapping("/{gameId}")
    fun getGameDetail(@PathVariable gameId: Long) =
        getGameDetailUseCase.execute(GetGameDetailCommand(gameId))
}