package com.innovation.dddexample.application.game

import com.innovation.dddexample.application.common.UseCase
import com.innovation.dddexample.domain.game.model.SeatStatus
import com.innovation.dddexample.domain.game.repository.GameRepository
import com.innovation.dddexample.domain.game.repository.SeatGradeRepository
import com.innovation.dddexample.domain.game.repository.SeatRepository
import com.innovation.dddexample.interfaces.dto.game.GetGameDetailResponse
import com.innovation.dddexample.interfaces.dto.game.SeatSummaryInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class GetGameDetailUseCase(
    private val gameRepository: GameRepository,
    private val seatRepository: SeatRepository,
    private val seatGradeRepository: SeatGradeRepository
) :
    UseCase<GetGameDetailCommand, GetGameDetailResponse> {
    override fun execute(command: GetGameDetailCommand): GetGameDetailResponse {
        val game = gameRepository.findById(command.gameId)
        val seats = seatRepository.findByGameId(command.gameId)
        val seatGrades = seatGradeRepository.findByGameId(command.gameId)

        val seatSummaryInfoList = mutableListOf<SeatSummaryInfo>()

        seatGrades.forEach { seatGrade ->
            val currentSeatList = seats.filter { it.seatGrade == seatGrade }
            val seatSummaryInfo = SeatSummaryInfo(seatGrade.name, 0, 0)

            currentSeatList.forEach { seat ->
                seatSummaryInfo.total++
                if (seat.status == SeatStatus.AVAILABLE) {
                    seatSummaryInfo.remaining++
                }
            }
            seatSummaryInfoList.add(seatSummaryInfo)
        }

        return GetGameDetailResponse(
            game!!.gameTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")),
            game.homeTeam.name,
            game.awayTeam.name,
            game.homeTeam.stadium,
            seatSummaryInfoList
        )
    }

}