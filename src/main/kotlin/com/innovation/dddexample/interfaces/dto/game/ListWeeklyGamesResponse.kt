package com.innovation.dddexample.interfaces.dto.game

data class ListWeeklyGamesResponse (
    val gameTime: String, // yyyy-MM-dd HH:mm
    val stadium: String,
    val homeTeam: String,
    val awayTeam: String,
    val isReserved: Boolean, // 내 예매여부,  TODO: 아직 티케팅 기능 개발 X. 추후 연동해야.
    val gameStatus: String, // 게임 상태 (시작전, 예약 만석, 종료된 게임)
)