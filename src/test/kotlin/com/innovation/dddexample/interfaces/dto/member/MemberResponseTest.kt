package com.innovation.dddexample.interfaces.dto.member

import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.model.Member
import com.innovation.dddexample.domain.member.model.PhoneNumber
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDateTime

@DisplayName("MemberResponse DTO 매핑 테스트")
class MemberResponseTest {

    @Test
    @DisplayName("활성 회원 정보가 올바르게 매핑되어야 한다")
    fun `toResponse should map active member correctly`() {
        // Given
        val member = Member(
            id = 1L,
            email = Email("test@example.com"),
            name = "홍길동",
            phoneNumber = PhoneNumber("01012345678"),
            registeredAt = LocalDateTime.of(2025, 1, 15, 10, 30),
            withdrawnAt = null,
            password = "hashedPassword123"
        )

        // When
        val response = member.toResponse()

        // Then
        assertEquals(1L, response.id)
        assertEquals("홍길동", response.name)
        assertEquals("test@example.com", response.email)
        assertEquals("010-****-5678", response.phoneNumber) // Corrected assertion
        assertEquals("ACTIVE", response.status)
        assertEquals(0, response.pointBalance)
        assertEquals(LocalDateTime.of(2025, 1, 15, 10, 30), response.createdAt)
    }

    @Test
    @DisplayName("탈퇴 회원 정보가 올바르게 매핑되어야 한다")
    fun `toResponse should map withdrawn member correctly`() {
        // Given
        val withdrawnTime = LocalDateTime.of(2024, 12, 31, 23, 59)
        val member = Member(
            id = 2L,
            email = Email("withdrawn@example.com"),
            name = "탈퇴회원",
            phoneNumber = PhoneNumber("01098765432"),
            registeredAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            withdrawnAt = withdrawnTime,
            password = "hashedPassword123"
        )

        // When
        val response = member.toResponse()

        // Then
        assertEquals(2L, response.id)
        assertEquals("탈퇴회원", response.name)
        assertEquals("WITHDRAWN", response.status)
        assertEquals("010-****-5432", response.phoneNumber) // Corrected assertion
    }

    @ParameterizedTest
    @CsvSource(
        "01011112222, 010-****-2222",
        "01099998888, 010-****-8888",
        "01012340000, 010-****-0000"
    )
    @DisplayName("전화번호 마스킹이 올바른 포맷을 유지해야 한다")
    fun `phone number masking should preserve format`(phone: String, expectedMasked: String) {
        // Given
        val member = Member(
            id = 1L,
            email = Email("test@example.com"),
            name = "Test",
            phoneNumber = PhoneNumber(phone),
            password = "hashedPassword123"
        )

        // When
        val response = member.toResponse()

        // Then
        assertEquals(expectedMasked, response.phoneNumber)
    }

    @Test
    @DisplayName("이메일이 getValue() 메소드를 통해 올바르게 매핑되어야 한다")
    fun `email should use getValue() method`() {
        // Given
        val testEmail = "user@domain.com"
        val member = Member(
            id = 1L,
            email = Email(testEmail),
            name = "User",
            phoneNumber = PhoneNumber("01011112222"),
            password = "hashedPassword123"
        )

        // When
        val response = member.toResponse()

        // Then
        assertEquals(testEmail, response.email)
    }
}
