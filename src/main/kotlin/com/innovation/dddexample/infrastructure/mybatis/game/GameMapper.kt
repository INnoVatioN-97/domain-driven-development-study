package com.innovation.dddexample.infrastructure.mybatis.game

import com.innovation.dddexample.interfaces.dto.game.ListWeeklyGamesResponse
import org.apache.ibatis.annotations.Mapper

@Mapper
interface GameMapper {

    fun findByDateRange(startDate: String, endDate: String):List<ListWeeklyGamesResponse>
}