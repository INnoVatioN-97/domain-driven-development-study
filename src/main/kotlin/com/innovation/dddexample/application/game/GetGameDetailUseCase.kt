package com.innovation.dddexample.application.game

import com.innovation.dddexample.application.common.UseCase
import com.innovation.dddexample.domain.game.exception.GameNotFoundException
import com.innovation.dddexample.domain.game.repository.GameRepository
import com.innovation.dddexample.domain.game.repository.SeatRepository
import com.innovation.dddexample.interfaces.dto.game.GetGameDetailResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class GetGameDetailUseCase(
    private val gameRepository: GameRepository,
    private val seatRepository: SeatRepository
) :
    UseCase<GetGameDetailCommand, GetGameDetailResponse> {
    override fun execute(command: GetGameDetailCommand): GetGameDetailResponse {
        val game = gameRepository.findGameDetailsById(command.gameId)
            ?: throw GameNotFoundException(command.gameId)

        val seatSummaryInfoList = seatRepository.findSeatSummaryByGameId(command.gameId)

        return GetGameDetailResponse(
            game.gameTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")),
            game.homeTeam.name,
            game.awayTeam.name,
            game.homeTeam.stadium,
            seatSummaryInfoList
        )
    }

}