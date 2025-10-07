package com.innovation.dddexample.domain.team.repository

import com.innovation.dddexample.domain.team.model.Team

interface TeamRepository {

    /**
     * 회원을 저장하거나 수정합니다. (생성 및 업데이트)
     */
    fun save(team: Team): Team
    fun saveAll(team: List<Team>): List<Team>

    /**
     * 고유 ID로 회원을 찾습니다.
     */
    fun findById(id: Long): Team?

    fun findAll(): List<Team>

}