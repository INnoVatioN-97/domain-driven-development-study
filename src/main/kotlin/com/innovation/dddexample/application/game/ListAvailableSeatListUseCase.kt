package com.innovation.dddexample.application.game

import com.innovation.dddexample.application.common.UseCase
import com.innovation.dddexample.domain.game.model.SeatStatus
import com.innovation.dddexample.domain.game.repository.SeatRepository
import com.innovation.dddexample.interfaces.dto.game.ListAvailableSeatListResponse
import com.innovation.dddexample.interfaces.dto.game.SeatInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListAvailableSeatListUseCase(
    private val seatRepository: SeatRepository
) : UseCase<ListAvailableSeatListCommand, ListAvailableSeatListResponse> {
    override fun execute(command: ListAvailableSeatListCommand): ListAvailableSeatListResponse {
        val seats = seatRepository.findByGameId(command.gameId)
            .filter { it.status == SeatStatus.AVAILABLE }
            .map { seat ->
                SeatInfo(
                    id = seat.id,
                    seatNumber = seat.seatNumber,
                    grade = seat.seatGrade.name,
                    price = seat.seatGrade.price
                )
            }

        return ListAvailableSeatListResponse(seats)
    }
}