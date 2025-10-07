package com.innovation.dddexample.domain.game.model

/**
 * 정규시즌 / 포스트시즌 등의 경기 타입을 나타내는 Enum 클래스입니다.
 */
enum class GameType(val description: String) {
    REGULAR_SEASON("졍규시즌"),
    POST_SEASON("포스트시즌")
}
