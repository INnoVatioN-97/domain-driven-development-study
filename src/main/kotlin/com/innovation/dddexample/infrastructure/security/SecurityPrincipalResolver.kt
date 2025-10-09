package com.innovation.dddexample.infrastructure.security

import com.innovation.dddexample.infrastructure.security.auth.MemberDetails
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityPrincipalResolver {

    /**
     * 현재 인증된 사용자의 Member ID를 반환합니다.
     * @return Member ID
     * @throws IllegalStateException 인증 정보가 없거나 유효하지 않을 경우
     */
    fun getMemberId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("Security context is empty.")

        // 익명 사용자인 경우 예외 처리
        if (authentication.principal == "anonymousUser") {
            throw IllegalStateException("User is not authenticated.")
        }

        if (authentication.principal is MemberDetails) {
            return (authentication.principal as MemberDetails).memberId
        }

        throw IllegalStateException("Invalid principal type. Expected MemberDetails.")
    }

    /**
     * 현재 인증된 사용자의 Member ID를 반환하거나, 인증되지 않은 경우 null을 반환합니다.
     * @return Member ID 또는 null
     */
    fun getMemberIdOrNull(): Long? {
        return try {
            getMemberId()
        } catch (e: IllegalStateException) {
            null
        }
    }

}
