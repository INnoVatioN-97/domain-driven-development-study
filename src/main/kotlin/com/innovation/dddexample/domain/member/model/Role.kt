package com.innovation.dddexample.domain.member.model

/**
 * 회원 권한을 나타내는 Enum 클래스입니다.
 * Spring Security에서는 권한을 "ROLE_" 접두사와 함께 사용합니다.
 */
enum class Role(val description: String) {
    USER("일반 사용자"),
    ADMIN("관리자")
}
