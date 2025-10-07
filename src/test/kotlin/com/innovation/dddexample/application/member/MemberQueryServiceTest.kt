package com.innovation.dddexample.application.member

import com.innovation.dddexample.domain.member.exception.MemberNotFoundException
import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.model.Member
import com.innovation.dddexample.domain.member.model.PhoneNumber
import com.innovation.dddexample.domain.member.repository.MemberRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * T005-T006: Unit tests for MemberQueryService
 *
 * Application Service의 핵심 로직을 검증합니다:
 * - Repository 호출 및 결과 반환
 * - 예외 처리 (Member not found)
 */
@DisplayName("MemberQueryService 단위 테스트")
class MemberQueryServiceTest {

    private val repository: MemberRepository = mockk()
    private val service = MemberQueryService(repository)

    @Test
    @DisplayName("ID로 회원 조회 시 존재하는 경우 회원 정보를 반환해야 한다")
    fun `getMemberById should return member when exists`() {
        // Given
        val expectedMember = Member(
            id = 1L,
            email = Email("test@example.com"),
            name = "Test User",
            phoneNumber = PhoneNumber("01012345678"),
            password = "hashedPassword123"
        )

        every { repository.findById(1L) } returns expectedMember

        // When
        val result = service.getMemberById(1L)

        // Then
        assertEquals(expectedMember, result)
        verify(exactly = 1) { repository.findById(1L) }
    }

    @Test
    @DisplayName("ID로 회원 조회 시 존재하지 않는 경우 MemberNotFoundException을 던져야 한다")
    fun `getMemberById should throw MemberNotFoundException when not exists`() {
        // Given
        every { repository.findById(999L) } returns null

        // When & Then
        val exception = assertThrows<MemberNotFoundException> {
            service.getMemberById(999L)
        }

        assertEquals("Member not found with id: 999", exception.message)
        verify(exactly = 1) { repository.findById(999L) }
    }

    @Test
    @DisplayName("올바른 ID로 Repository를 호출해야 한다")
    fun `service should call repository with correct ID`() {
        // Given
        val member = Member(
            id = 42L,
            email = Email("user@test.com"),
            name = "User",
            phoneNumber = PhoneNumber("01099998888"),
            password = "hashedPassword123"
        )

        every { repository.findById(42L) } returns member

        // When
        service.getMemberById(42L)

        // Then
        verify(exactly = 1) { repository.findById(42L) }
    }
}