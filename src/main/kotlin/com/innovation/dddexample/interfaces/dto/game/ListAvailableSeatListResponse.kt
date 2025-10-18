package com.innovation.dddexample.interfaces.dto.game

import java.math.BigDecimal

data class ListAvailableSeatListResponse(
    val seats: List<SeatInfo>
)

data class SeatInfo(
    val id: Long,
    val seatNumber: String,
    val grade: String,
    val price: BigDecimal
)