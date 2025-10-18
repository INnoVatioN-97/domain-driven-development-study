package com.innovation.dddexample.infrastructure.persistence.game

import com.innovation.dddexample.domain.game.model.SeatGrade
import org.springframework.data.jpa.repository.JpaRepository

interface SeatGradeJpaRepository : JpaRepository<SeatGrade, Long>
