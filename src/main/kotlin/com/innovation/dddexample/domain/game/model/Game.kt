package com.innovation.dddexample.domain.game.model

import com.innovation.dddexample.domain.team.model.Team
import jakarta.persistence.*
import java.time.LocalDate

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

    // "2025-09-02 두산 vs KT
    @Column(nullable = false, length = 40)
    val title: String,

    // 홈팀
    @ManyToOne
    @JoinColumn(nullable = false, name = "home_team_id", referencedColumnName = "id")
    val homeTeam: Team,

    // 어웨이팀
    @ManyToOne
    @JoinColumn(nullable = false, name = "away_team_id", referencedColumnName = "id")
    val awayTeam: Team,

    // 경기일자
    @Column(nullable = false)
    val gameDate: LocalDate,

    // 전적


)