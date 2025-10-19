package com.innovation.dddexample.interfaces.dto.game

data class GetGameDetailResponse(
    val gameTime: String,
    val homeTeam: String,
    val awayTeam: String,
    val stadium: String,
    val seatsSummary: List<SeatSummaryInfo>
)

data class SeatSummaryInfo(var grade: String, var total: Long, var remaining: Long)