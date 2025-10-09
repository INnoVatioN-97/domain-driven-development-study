package com.innovation.dddexample.application.game

import com.innovation.dddexample.application.common.UseCase
import com.innovation.dddexample.domain.game.repository.GameRepository
import com.innovation.dddexample.domain.member.repository.MemberRepository
import com.innovation.dddexample.domain.team.repository.TeamRepository
import com.innovation.dddexample.infrastructure.mybatis.game.GameMapper
import com.innovation.dddexample.interfaces.dto.game.ListWeeklyGamesResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/**
 * 주간 경기일정 목록 조회 유즈케이스
 */
@Service
@Transactional(readOnly = true)
class ListWeeklyGamesUseCase(
    private val gameMapper: GameMapper,
    private val gameRepository: GameRepository,
    private val memberRepository: MemberRepository,
    private val teamRepository: TeamRepository
) : UseCase<ListWeeklyGamesCommand, List<ListWeeklyGamesResponse>> {
    override fun execute(command: ListWeeklyGamesCommand): List<ListWeeklyGamesResponse> {
        val weekStartDate =
            LocalDate.parse(command.date, DateTimeFormatter.ISO_DATE).with(DayOfWeek.MONDAY)
                .format(DateTimeFormatter.ISO_DATE)
        val weekEndDate =
            LocalDate.parse(command.date, DateTimeFormatter.ISO_DATE).with(DayOfWeek.SUNDAY)
                .format(DateTimeFormatter.ISO_DATE)

        val games = gameMapper.findByDateRange(weekStartDate, weekEndDate)

        return games
    }

}