package com.innovation.dddexample.infrastructure.seed.team

import com.innovation.dddexample.domain.team.model.Team
import com.innovation.dddexample.domain.team.repository.TeamRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class TeamSeeder(
    private val teamRepository: TeamRepository
) {

    @Transactional
    fun seedTeams() {

        logger.info { "Seeding teams" }

        val teamList = listOf(
            Team(
                title = "KIA 타이거즈",
                logoUrl = "https://www.tigers.co.kr/img/common/logo_tigers.png",
                stadium = "광주기아챔피언스필드"
            ),
            Team(
                title = "삼성 라이온즈",
                logoUrl = "https://www.samsunglions.com/img/common/logo_lions.png",
                stadium = "대구삼성라이온즈파크"
            ),
            Team(
                title = "LG 트윈스",
                logoUrl = "https://www.lgtwins.com/images/common/logo_twins.png",
                stadium = "서울종합운동장 야구장"
            ),
            Team(
                title = "두산 베어스",
                logoUrl = "https://www.doosanbears.com/images/common/logo_doosanbears.png",
                stadium = "서울종합운동장 야구장"
            ),
            Team(
                title = "kt wiz",
                logoUrl = "https://www.ktwiz.co.kr/images/common/logo_wiz.png",
                stadium = "수원KT위즈파크"
            ),
            Team(
                title = "SSG 랜더스",
                logoUrl = "https://www.ssglanders.com/images/common/logo_landers.png",
                stadium = "인천SSG랜더스필드"
            ),
            Team(
                title = "롯데 자이언츠",
                logoUrl = "https://www.giantsclub.com/images/common/logo_giants.png",
                stadium = "사직 야구장"
            ),
            Team(
                title = "한화 이글스",
                logoUrl = "https://www.hanwhaeagles.co.kr/images/common/logo_eagles.png",
                stadium = "대전한화생명이글스파크"
            ),
            Team(
                title = "NC 다이노스",
                logoUrl = "https://www.ncdinos.com/images/common/logo_dinos.png",
                stadium = "창원NC파크"
            ),
            Team(
                title = "키움 히어로즈",
                logoUrl = "https://www.heroesbaseball.co.kr/images/common/logo_heroes.png",
                stadium = "고척스카이돔"
            )
        )

        teamRepository.saveAll(teamList)

        logger.info { "Finished seeding ${teamList.size} teams" }
    }
}