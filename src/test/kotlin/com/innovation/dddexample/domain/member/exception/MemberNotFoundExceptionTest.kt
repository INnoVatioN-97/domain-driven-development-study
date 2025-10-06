package com.innovation.dddexample.domain.member.exception

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * T004: Unit test for MemberNotFoundException
 *
 * 도메인 예외가 올바르게 동작하는지 검증합니다.
 */
class MemberNotFoundExceptionTest : FunSpec({

    test("exception message should contain member ID") {
        // Given
        val memberId = 999L

        // When
        val exception = MemberNotFoundException.byId(memberId)

        // Then
        exception.message shouldContain "999"
        exception.message shouldContain "Member not found"
    }

    test("exception should be RuntimeException subclass") {
        // Given
        val exception = MemberNotFoundException.byId(123L)

        // Then
        exception.shouldBeInstanceOf<RuntimeException>()
    }

    test("different member IDs should produce different messages") {
        // Given
        val exception1 = MemberNotFoundException.byId(1L)
        val exception2 = MemberNotFoundException.byId(2L)

        // Then
        exception1.message shouldContain "1"
        exception2.message shouldContain "2"
        (exception1.message == exception2.message) shouldBe false
    }
})
