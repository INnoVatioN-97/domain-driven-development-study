package com.innovation.dddexample.domain.game.repository

import com.innovation.dddexample.interfaces.dto.game.SeatSummaryInfo

interface SeatRepositoryCustom {
    fun findSeatSummaryByGameId(gameId: Long): List<SeatSummaryInfo>
}
