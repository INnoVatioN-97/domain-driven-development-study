package com.innovation.dddexample.interfaces.dto.member

import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.model.Member
import com.innovation.dddexample.domain.member.model.PhoneNumber
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

/**
 * T007-T008: Unit tests for MemberResponse DTO mapping
 *
 * DTO 변환 로직이 올바르게 동작하는지 검증합니다:
 * - Email Value Object → String 변환
 * - PhoneNumber → 마스킹된 String 변환
 * - 회원 상태 파생 (ACTIVE/WITHDRAWN)
 */
class MemberResponseTest : FunSpec({

    test("T007: toResponse should map active member correctly") {
        // Given
        val member = Member(
            id = 1L,
            email = Email("test@example.com"),
            name = "홍길동",
            phoneNumber = PhoneNumber("01012345678"),
            registeredAt = LocalDateTime.of(2025, 1, 15, 10, 30),
            withdrawnAt = null
        )

        // When
        val response = member.toResponse()

        // Then
        response.id shouldBe 1L
        response.name shouldBe "홍길동"
        response.email shouldBe "test@example.com"
        response.phoneNumber shouldBe "0101****678" // MASKED! (raw format without hyphens)
        response.status shouldBe "ACTIVE"
        response.pointBalance shouldBe 0
        response.createdAt shouldBe LocalDateTime.of(2025, 1, 15, 10, 30)
    }

    test("T008: toResponse should map withdrawn member correctly") {
        // Given
        val withdrawnTime = LocalDateTime.of(2024, 12, 31, 23, 59)
        val member = Member(
            id = 2L,
            email = Email("withdrawn@example.com"),
            name = "탈퇴회원",
            phoneNumber = PhoneNumber("01098765432"),
            registeredAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            withdrawnAt = withdrawnTime
        )

        // When
        val response = member.toResponse()

        // Then
        response.id shouldBe 2L
        response.name shouldBe "탈퇴회원"
        response.status shouldBe "WITHDRAWN" // Different from active!
        response.phoneNumber shouldBe "0109****432" // Still masked (raw format)
    }

    test("phone number masking should preserve format") {
        // Given: Different phone numbers
        val testCases = listOf(
            "01011112222" to "0101****222",
            "01099998888" to "0109****888",
            "01012340000" to "0101****000"
        )

        testCases.forEach { (phone, expectedMasked) ->
            // Given
            val member = Member(
                id = 1L,
                email = Email("test@example.com"),
                name = "Test",
                phoneNumber = PhoneNumber(phone)
            )

            // When
            val response = member.toResponse()

            // Then
            response.phoneNumber shouldBe expectedMasked
        }
    }

    test("email should use getValue() method") {
        // Given
        val testEmail = "user@domain.com"
        val member = Member(
            id = 1L,
            email = Email(testEmail),
            name = "User",
            phoneNumber = PhoneNumber("01011112222")
        )

        // When
        val response = member.toResponse()

        // Then
        response.email shouldBe testEmail
    }
})
