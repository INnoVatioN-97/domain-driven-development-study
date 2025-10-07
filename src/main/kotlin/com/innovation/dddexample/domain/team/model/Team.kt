package com.innovation.dddexample.domain.team.model

import com.innovation.dddexample.domain.game.model.Game
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

/**
 * 경기 엔티티
 */
@Entity
@Table(name = "teams")
class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 45)
    val title: String,

    @Column(nullable = false, length = 256)
    val logoUrl: String,

    // 경기장명
    @Column(nullable = false, length = 45)
    val stadium: String,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "homeTeam")
    val homeGames: MutableList<Game> = mutableListOf(),

    @OneToMany(mappedBy = "awayTeam")
    val awayGames: MutableList<Game> = mutableListOf(),

    @OneToMany(mappedBy = "winner")
    val winningGames: MutableList<Game> = mutableListOf(),

    @OneToMany(mappedBy = "loser")
    val losingGames: MutableList<Game> = mutableListOf(),

    ) {


}