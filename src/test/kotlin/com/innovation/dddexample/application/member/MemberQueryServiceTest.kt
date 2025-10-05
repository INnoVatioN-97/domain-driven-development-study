package com.innovation.dddexample.application.member

import com.innovation.dddexample.domain.member.exception.MemberNotFoundException
import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.model.Member
import com.innovation.dddexample.domain.member.model.PhoneNumber
import com.innovation.dddexample.domain.member.repository.MemberRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

/**
 * T005-T006: Unit tests for MemberQueryService
 *
 * Application Service의 핵심 로직을 검증합니다:
 * - Repository 호출 및 결과 반환
 * - 예외 처리 (Member not found)
 */
class MemberQueryServiceTest : FunSpec({

    test("T005: getMemberById should return member when exists") {
        // Given
        val repository = mockk<MemberRepository>()
        val service = MemberQueryService(repository)

        val expectedMember = Member(
            id = 1L,
            email = Email("test@example.com"),
            name = "Test User",
            phoneNumber = PhoneNumber("01012345678")
        )

        every { repository.findById(1L) } returns expectedMember

        // When
        val result = service.getMemberById(1L)

        // Then
        result shouldBe expectedMember
        verify(exactly = 1) { repository.findById(1L) }
    }

    test("T006: getMemberById should throw MemberNotFoundException when not exists") {
        // Given
        val repository = mockk<MemberRepository>()
        val service = MemberQueryService(repository)

        every { repository.findById(999L) } returns null

        // When & Then
        val exception = shouldThrow<MemberNotFoundException> {
            service.getMemberById(999L)
        }

        exception.message shouldContain "999"
        exception.message shouldContain "Member not found"
        verify(exactly = 1) { repository.findById(999L) }
    }

    test("service should call repository with correct ID") {
        // Given
        val repository = mockk<MemberRepository>()
        val service = MemberQueryService(repository)
        val member = Member(
            id = 42L,
            email = Email("user@test.com"),
            name = "User",
            phoneNumber = PhoneNumber("01099998888")
        )

        every { repository.findById(42L) } returns member

        // When
        service.getMemberById(42L)

        // Then
        verify(exactly = 1) { repository.findById(42L) }
    }
})
