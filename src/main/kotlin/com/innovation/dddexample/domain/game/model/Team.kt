package com.innovation.dddexample.domain.game.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

/**
 * 팀 엔티티
 */
@Entity
@Table(name = "teams")
class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false, length = 45)
    val name: String,

    @Column(nullable = false, length = 256)
    val logoUrl: String,

    // 경기장명
    @Column(nullable = false, length = 45)
    val stadium: String,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

) {
    // Team과 관련된 비즈니스 로직을 여기에 추가할 수 있습니다.
}
