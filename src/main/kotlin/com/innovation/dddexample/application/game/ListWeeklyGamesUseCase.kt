package com.innovation.dddexample.application.game

import com.innovation.dddexample.application.common.UseCase
import com.innovation.dddexample.domain.game.repository.GameRepository
import com.innovation.dddexample.domain.reservation.repository.ReservationRepository
import com.innovation.dddexample.infrastructure.security.SecurityPrincipalResolver
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
    private val securityPrincipalResolver: SecurityPrincipalResolver
) : UseCase<ListWeeklyGamesCommand, List<ListWeeklyGamesResponse>> {
    override fun execute(command: ListWeeklyGamesCommand): List<ListWeeklyGamesResponse> {

        val weekStartDate =
            LocalDate.parse(command.date, DateTimeFormatter.ISO_DATE).with(DayOfWeek.MONDAY).atStartOfDay()
        val weekEndDate =
            LocalDate.parse(command.date, DateTimeFormatter.ISO_DATE).with(DayOfWeek.SUNDAY).atTime(23, 59, 59)

        // 1. Game과 Team 정보를 한 번의 쿼리로 가져옵니다. (최적화 완료)
        val games = gameRepository.findByDateRange(weekStartDate, weekEndDate)
        if (games.isEmpty()) return emptyList()

        // 2. 인증된 사용자의 예매 정보만 한 번의 쿼리로 가져옵니다. (최적화 완료)
        val memberId = securityPrincipalResolver.getMemberIdOrNull()
        val reservedGameIds: Set<Long> = if (memberId != null) {
            val gameIds = games.mapNotNull { it.id }
            reservationRepository.findByMemberIdAndGameIds(memberId, gameIds)
                .map { it.gameId }
                .toSet()
        } else {
            emptySet()
        }

        // 3. UseCase가 DTO 변환을 책임집니다.
        return games.map { game ->
            ListWeeklyGamesResponse(
                gameTime = game.gameTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")),
                stadium = game.homeTeam.stadium, // fetch join으로 가져왔으므로 바로 접근 가능
                homeTeam = game.homeTeam.title,
                awayTeam = game.awayTeam.title,
                isReserved = reservedGameIds.contains(game.id),
                gameStatus = if (game.gameTime.isBefore(java.time.LocalDateTime.now())) "CLOSED" else "NOT_STARTED"
            )
        }
    }
}