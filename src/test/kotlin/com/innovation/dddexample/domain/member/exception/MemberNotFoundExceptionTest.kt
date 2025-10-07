package com.innovation.dddexample.domain.member.exception

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * T004: Unit test for MemberNotFoundException
 *
 * 도메인 예외가 올바르게 동작하는지 검증합니다.
 */
@DisplayName("MemberNotFoundException 단위 테스트")
class MemberNotFoundExceptionTest {

    @Test
    @DisplayName("예외 메시지에 회원 ID가 포함되어야 한다")
    fun `exception message should contain member ID`() {
        // Given
        val memberId = 999L

        // When
        val exception = MemberNotFoundException.byId(memberId)

        // Then
        assertTrue(exception.message!!.contains("999"))
        assertTrue(exception.message!!.contains("Member not found"))
    }

    @Test
    @DisplayName("RuntimeException의 하위 클래스여야 한다")
    fun `exception should be RuntimeException subclass`() {
        // Given
        val exception = MemberNotFoundException.byId(123L)

        // Then
        assertTrue(exception is RuntimeException)
    }

    @Test
    @DisplayName("다른 회원 ID는 다른 예외 메시지를 생성해야 한다")
    fun `different member IDs should produce different messages`() {
        // Given
        val exception1 = MemberNotFoundException.byId(1L)
        val exception2 = MemberNotFoundException.byId(2L)

        // Then
        assertTrue(exception1.message!!.contains("1"))
        assertTrue(exception2.message!!.contains("2"))
        assertNotEquals(exception1.message, exception2.message)
    }
}