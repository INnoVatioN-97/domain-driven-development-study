package com.innovation.dddexample.interfaces.rest.member

import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.model.Member
import com.innovation.dddexample.domain.member.model.PhoneNumber
import com.innovation.dddexample.domain.member.repository.MemberRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

/**
 * MemberController 통합 테스트
 *
 * [테스트 범위]
 * - GET /members/{id} - 회원 조회
 *
 * [통합 테스트 특징]
 * - @SpringBootTest: 전체 Spring 컨텍스트 로딩
 * - @AutoConfigureMockMvc: Security 필터 비활성화로 인증 없이 테스트
 * - @Transactional: 각 테스트 후 자동 롤백
 * - 실제 DB 사용 (MySQL)
 *
 * [검증 항목]
 * 1. 회원 ID로 조회 성공
 * 2. 존재하지 않는 회원 조회 시 404 반환
 * 3. 탈퇴한 회원 조회 시 WITHDRAWN 상태 반환
 * 4. 전화번호 마스킹 검증
 * 5. 잘못된 ID 형식 처리
 * 6. DTO 변환 검증
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("MemberController 통합 테스트")
class MemberControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var testMember: Member

    @BeforeEach
    fun setUp() {
        // 테스트용 회원 생성
        testMember = memberRepository.save(
            Member(
                email = Email("testuser@example.com"),
                name = "테스트유저",
                phoneNumber = PhoneNumber("01012345678"),
                password = passwordEncoder.encode("Password123!")
            )
        )
    }

    @Nested
    @DisplayName("GET /members/{id} - 회원 조회")
    inner class GetMemberTests {

        @Test
        @DisplayName("존재하는 회원 ID로 조회 성공 - 200 OK, 회원 정보 반환")
        fun `should return 200 and member data when member exists`() {
            // When
            val result = mockMvc.get("/members/${testMember.id}")

            // Then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.id") { value(testMember.id) }
                jsonPath("$.name") { value("테스트유저") }
                jsonPath("$.email") { value("testuser@example.com") }
                jsonPath("$.phoneNumber") { exists() }
                jsonPath("$.status") { value("ACTIVE") }
                jsonPath("$.pointBalance") { value(0) }
            }
        }

        @Test
        @DisplayName("전화번호 마스킹 검증 - 중간 4자리가 마스킹됨")
        fun `should return masked phone number`() {
            // When
            val result = mockMvc.get("/members/${testMember.id}")

            // Then
            result.andExpect {
                status { isOk() }
                jsonPath("$.phoneNumber") { value("010-****-5678") }
            }
        }

        @Test
        @DisplayName("존재하지 않는 회원 ID로 조회 - 404 Not Found 반환")
        fun `should return 404 when member does not exist`() {
            // Given
            val nonExistentId = 999999L

            // When
            val result = mockMvc.get("/members/$nonExistentId")

            // Then
            result.andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(404) }
                jsonPath("$.message") { value("Member not found with id: $nonExistentId") }
            }
        }

        @Test
        @DisplayName("잘못된 ID 형식으로 조회 - 400 Bad Request 반환")
        fun `should return 400 when ID format is invalid`() {
            // When
            val result = mockMvc.get("/members/invalid-id")

            // Then
            result.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("음수 ID로 조회 - 404 Not Found 반환")
        fun `should return 404 when ID is negative`() {
            // When
            val result = mockMvc.get("/members/-1")

            // Then
            result.andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @DisplayName("0 ID로 조회 - 404 Not Found 반환")
        fun `should return 404 when ID is zero`() {
            // When
            val result = mockMvc.get("/members/0")

            // Then
            result.andExpect {
                status { isNotFound() }
            }
        }
    }

    @Nested
    @DisplayName("탈퇴한 회원 조회")
    inner class WithdrawnMemberTests {

        @Test
        @DisplayName("탈퇴한 회원 조회 시 WITHDRAWN 상태로 반환")
        fun `should return WITHDRAWN status for withdrawn member`() {
            // Given: 회원 탈퇴 처리
            val withdrawnMember = memberRepository.save(
                Member(
                    email = Email("withdrawn@example.com"),
                    name = "탈퇴회원",
                    phoneNumber = PhoneNumber("01099998888"),
                    password = passwordEncoder.encode("Password123!")
                )
            )

            val member = memberRepository.findById(withdrawnMember.id!!)
            member?.withdraw()
            memberRepository.save(member!!)

            // When
            val result = mockMvc.get("/members/${withdrawnMember.id}")

            // Then
            result.andExpect {
                status { isOk() }
                jsonPath("$.status") { value("WITHDRAWN") }
                jsonPath("$.id") { value(withdrawnMember.id) }
                jsonPath("$.name") { value("탈퇴회원") }
            }
        }

        @Test
        @DisplayName("탈퇴한 회원도 전화번호 마스킹 적용")
        fun `should mask phone number even for withdrawn member`() {
            // Given: 회원 탈퇴 처리
            val withdrawnMember = memberRepository.save(
                Member(
                    email = Email("withdrawn-mask@example.com"),
                    name = "탈퇴마스킹",
                    phoneNumber = PhoneNumber("01077778888"),
                    password = passwordEncoder.encode("Password123!")
                )
            )

            val member = memberRepository.findById(withdrawnMember.id!!)
            member?.withdraw()
            memberRepository.save(member!!)

            // When
            val result = mockMvc.get("/members/${withdrawnMember.id}")

            // Then
            result.andExpect {
                status { isOk() }
                jsonPath("$.phoneNumber") { value("010-****-8888") }
                jsonPath("$.status") { value("WITHDRAWN") }
            }
        }
    }

    @Nested
    @DisplayName("다양한 Value Object 조합 검증")
    inner class ValueObjectTests {

        @Test
        @DisplayName("다양한 전화번호 형식 모두 정규화되어 저장됨")
        fun `should normalize various phone number formats`() {
            // Given: 다양한 형식의 전화번호로 회원 생성
            val member1 = memberRepository.save(
                Member(
                    email = Email("phone1@example.com"),
                    name = "전화1",
                    phoneNumber = PhoneNumber("01011112222"), // 하이픈 없음
                    password = "password"
                )
            )

            val member2 = memberRepository.save(
                Member(
                    email = Email("phone2@example.com"),
                    name = "전화2",
                    phoneNumber = PhoneNumber("010-3333-4444"), // 하이픈 있음
                    password = "password"
                )
            )

            // When & Then
            mockMvc.get("/members/${member1.id}").andExpect {
                status { isOk() }
                jsonPath("$.phoneNumber") { value("010-****-2222") }
            }

            mockMvc.get("/members/${member2.id}").andExpect {
                status { isOk() }
                jsonPath("$.phoneNumber") { value("010-****-4444") }
            }
        }

        // NOTE: Email Value Object는 현재 대소문자를 그대로 저장함
        // 대소문자 정규화가 필요하면 Email 클래스 수정 필요

        @Test
        @DisplayName("이메일이 올바르게 저장되고 조회됨")
        fun `should store and retrieve email correctly`() {
            // Given: 다른 이메일로 새 회원 생성
            val member = memberRepository.save(
                Member(
                    email = Email("emailtest@example.com"),
                    name = "이메일테스트",
                    phoneNumber = PhoneNumber("01055556666"),
                    password = "password"
                )
            )

            // When
            val result = mockMvc.get("/members/${member.id}")

            // Then
            result.andExpect {
                status { isOk() }
                jsonPath("$.email") { value("emailtest@example.com") }
            }
        }
    }

    @Nested
    @DisplayName("DTO 변환 검증")
    inner class DtoConversionTests {

        @Test
        @DisplayName("Member 엔티티가 MemberResponse DTO로 올바르게 변환됨")
        fun `should convert Member entity to MemberResponse DTO correctly`() {
            // When
            val result = mockMvc.get("/members/${testMember.id}").andReturn()

            // Then
            val responseBody = result.response.contentAsString
            val memberResponse = objectMapper.readTree(responseBody)

            assertThat(memberResponse.has("id")).isTrue
            assertThat(memberResponse.has("name")).isTrue
            assertThat(memberResponse.has("email")).isTrue
            assertThat(memberResponse.has("phoneNumber")).isTrue
            assertThat(memberResponse.has("status")).isTrue
            assertThat(memberResponse.has("pointBalance")).isTrue

            // 도메인 엔티티의 내부 정보는 노출되지 않음
            assertThat(memberResponse.has("password")).isFalse
            assertThat(memberResponse.has("deletedAt")).isFalse
        }

        @Test
        @DisplayName("응답 JSON에 password 필드가 포함되지 않음")
        fun `should not include password in response`() {
            // When
            val result = mockMvc.get("/members/${testMember.id}").andReturn()

            // Then
            val responseBody = result.response.contentAsString
            assertThat(responseBody).doesNotContain("password")
            assertThat(responseBody).doesNotContain("Password")
        }

        @Test
        @DisplayName("JSON 응답 형식이 올바름")
        fun `should return valid JSON format`() {
            // When
            val result = mockMvc.get("/members/${testMember.id}")

            // Then
            result.andExpect {
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json("""
                        {
                            "id": ${testMember.id},
                            "name": "테스트유저",
                            "email": "testuser@example.com",
                            "phoneNumber": "010-****-5678",
                            "status": "ACTIVE",
                            "pointBalance": 0
                        }
                    """.trimIndent(), strict = false)
                }
            }
        }
    }

    @Nested
    @DisplayName("성능 및 일관성 검증")
    inner class PerformanceAndConsistencyTests {

        @Test
        @DisplayName("동일 회원을 여러 번 조회해도 일관된 결과 반환")
        fun `should return consistent results for multiple queries`() {
            // When: 동일 회원을 3번 조회
            val result1 = mockMvc.get("/members/${testMember.id}").andReturn().response.contentAsString
            val result2 = mockMvc.get("/members/${testMember.id}").andReturn().response.contentAsString
            val result3 = mockMvc.get("/members/${testMember.id}").andReturn().response.contentAsString

            // Then: 모든 결과가 동일함
            assertThat(result1).isEqualTo(result2)
            assertThat(result2).isEqualTo(result3)
        }

        @Test
        @DisplayName("여러 회원 생성 후 각각 정확히 조회 가능")
        fun `should be able to query each member independently`() {
            // Given: 여러 회원 생성
            val members = (1..5).map { i ->
                memberRepository.save(
                    Member(
                        email = Email("user$i@example.com"),
                        name = "사용자$i",
                        phoneNumber = PhoneNumber("010${String.format("%08d", i)}"),
                        password = "password"
                    )
                )
            }

            // When & Then: 각 회원을 정확히 조회 가능
            members.forEach { member ->
                mockMvc.get("/members/${member.id}").andExpect {
                    status { isOk() }
                    jsonPath("$.id") { value(member.id) }
                    jsonPath("$.email") { value(member.email.value) }
                }
            }
        }

        @Test
        @DisplayName("트랜잭션 롤백으로 테스트 격리 검증")
        fun `should isolate tests with transaction rollback`() {
            // Given: 새 회원 생성
            val newMember = memberRepository.save(
                Member(
                    email = Email("isolation@example.com"),
                    name = "격리테스트",
                    phoneNumber = PhoneNumber("01012121212"),
                    password = "password"
                )
            )

            // When: 회원 조회
            mockMvc.get("/members/${newMember.id}").andExpect {
                status { isOk() }
            }

            // Then: @Transactional에 의해 테스트 종료 후 자동 롤백됨
            // 다음 테스트에서는 이 회원이 존재하지 않음
        }
    }

    @Nested
    @DisplayName("예외 처리 및 에러 응답")
    inner class ErrorHandlingTests {

        @Test
        @DisplayName("GlobalExceptionHandler가 MemberNotFoundException을 404로 변환")
        fun `should convert MemberNotFoundException to 404 response`() {
            // When
            val result = mockMvc.get("/members/999999")

            // Then
            result.andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(404) }
                jsonPath("$.message") { exists() }
            }
        }

        @Test
        @DisplayName("에러 응답에 적절한 메시지 포함")
        fun `should include meaningful error message in response`() {
            // Given
            val nonExistentId = 12345L

            // When
            val result = mockMvc.get("/members/$nonExistentId")

            // Then
            result.andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Member not found with id: $nonExistentId") }
            }
        }

        @Test
        @DisplayName("에러 응답이 올바른 JSON 형식")
        fun `should return error response in valid JSON format`() {
            // When
            val result = mockMvc.get("/members/999999").andReturn()

            // Then
            val errorResponse = objectMapper.readTree(result.response.contentAsString)
            assertThat(errorResponse.has("status")).isTrue
            assertThat(errorResponse.has("message")).isTrue
            assertThat(errorResponse.get("status").asInt()).isEqualTo(404)
        }
    }
}
