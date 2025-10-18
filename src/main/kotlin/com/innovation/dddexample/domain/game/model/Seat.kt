package com.innovation.dddexample.domain.game.model

import jakarta.persistence.*

@Entity
@Table(name = "seats")
class Seat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    val game: Game,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_grade_id", nullable = false)
    val seatGrade: SeatGrade,

    @Column(name = "seat_number", nullable = false)
    val seatNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: SeatStatus = SeatStatus.AVAILABLE
) {
    fun reserve() {
        if (this.status != SeatStatus.AVAILABLE && this.status != SeatStatus.SELECTED) {
            throw IllegalStateException("Seat is not available for reservation.")
        }
        this.status = SeatStatus.RESERVED
    }

    fun select() {
        if (this.status != SeatStatus.AVAILABLE) {
            throw IllegalStateException("Seat is not available for selection.")
        }
        this.status = SeatStatus.SELECTED
    }

    fun makeAvailable() {
        this.status = SeatStatus.AVAILABLE
    }
}
