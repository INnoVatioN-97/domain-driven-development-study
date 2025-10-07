package com.innovation.dddexample.domain.game.model

import com.innovation.dddexample.domain.team.model.Team
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 경기 엔티티
 */
@Entity
@Table(name = "games")
class Game(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val gameType: GameType = GameType.REGULAR_SEASON,

    // 홈팀
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "home_team_id", referencedColumnName = "id")
    val homeTeam: Team,

    // 어웨이팀
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "away_team_id", referencedColumnName = "id")
    val awayTeam: Team,

    // 경기 진행일시
    @Column(nullable = false)
    val gameTime: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = "winner_id", referencedColumnName = "id")
    val winner: Team? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = "loser_id", referencedColumnName = "id")
    val loser: Team? = null,
) {
    init {
        require(homeTeam.id != awayTeam.id) { "Home team and away team cannot be the same." }
    }
}