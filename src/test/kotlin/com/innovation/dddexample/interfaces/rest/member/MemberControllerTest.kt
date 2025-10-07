package com.innovation.dddexample.interfaces.rest.member

import com.innovation.dddexample.application.member.MemberQueryService
import com.innovation.dddexample.domain.member.exception.MemberNotFoundException
import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.model.Member
import com.innovation.dddexample.domain.member.model.PhoneNumber
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var memberQueryService: MemberQueryService

    @Test
    fun `T009 - GET member by ID should return 200 OK with member data`() {
        // Given
        val member = Member(
            id = 1L,
            email = Email("hong@example.com"),
            name = "홍길동",
            phoneNumber = PhoneNumber("01012345678"),
            password = "hashedPassword123"
        )

        every { memberQueryService.getMemberById(1L) } returns member

        // When & Then
        mockMvc.get("/members/1")
            .andExpect {
                status { isOk() }
                content { contentType("application/json") }
                jsonPath("$.id") { value(1) }
                jsonPath("$.name") { value("홍길동") }
                jsonPath("$.email") { value("hong@example.com") }
                jsonPath("$.phoneNumber") { value("010-****-5678") }
                jsonPath("$.status") { value("ACTIVE") }
                jsonPath("$.pointBalance") { value(0) }
            }
    }

    @Test
    fun `T010 - GET member by ID should return 404 when member not found`() {
        // Given
        every { memberQueryService.getMemberById(999L) } throws MemberNotFoundException.byId(999L)

        // When & Then
        mockMvc.get("/members/999")
            .andExpect {
                status { isNotFound() }
                content { contentType("application/json") }
                jsonPath("$.status") { value(404) }
                jsonPath("$.message") { value("Member not found with id: 999") }
            }
    }

    @Test
    fun `T011 - GET member with invalid ID format should return 400`() {
        // When & Then
        mockMvc.get("/members/abc")
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `withdrawn member should have WITHDRAWN status`() {
        // Given
        val withdrawnMember = Member(
            id = 2L,
            email = Email("withdrawn@example.com"),
            name = "탈퇴회원",
            phoneNumber = PhoneNumber("01098765432"),
            password = "hashedPassword123"
        )
        withdrawnMember.withdraw() // Mark as withdrawn

        every { memberQueryService.getMemberById(2L) } returns withdrawnMember

        // When & Then
        mockMvc.get("/members/2")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("WITHDRAWN") }
                jsonPath("$.phoneNumber") { value("010-****-5432") }
            }
    }
}