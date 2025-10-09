package com.innovation.dddexample.interfaces.rest.game

import com.innovation.dddexample.application.game.ListWeeklyGamesCommand
import com.innovation.dddexample.application.game.ListWeeklyGamesUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/games")
class GameController(private val listWeeklyGamesUseCase: ListWeeklyGamesUseCase) {

    @GetMapping
    fun listWeeklyGames(@RequestParam("date") date: String) =
        listWeeklyGamesUseCase.execute(ListWeeklyGamesCommand(date))
}