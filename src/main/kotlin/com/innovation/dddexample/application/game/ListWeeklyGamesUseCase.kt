package com.innovation.dddexample.application.game

import com.innovation.dddexample.application.common.UseCase
import com.innovation.dddexample.domain.game.repository.GameRepository
import com.innovation.dddexample.domain.member.repository.MemberRepository
import com.innovation.dddexample.domain.reservation.repository.ReservationRepository
import com.innovation.dddexample.domain.team.repository.TeamRepository
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
    private val gameRepository: GameRepository,
    private val reservationRepository: ReservationRepository,
    private val memberRepository: MemberRepository,
    private val teamRepository: TeamRepository
) : UseCase<ListWeeklyGamesCommand, List<ListWeeklyGamesResponse>> {
    override fun execute(command: ListWeeklyGamesCommand): List<ListWeeklyGamesResponse> {
        val weekStartDate =
            LocalDate.parse(command.date, DateTimeFormatter.ISO_DATE).with(DayOfWeek.MONDAY).atTime(0, 0)
        val weekEndDate =
            LocalDate.parse(command.date, DateTimeFormatter.ISO_DATE).with(DayOfWeek.SUNDAY).atTime(23, 59)

        // 주간 일정 전체 가져오기
        val games = gameRepository.findByDateRange(weekStartDate, weekEndDate)

        return emptyList()
    }

}