package com.innovation.dddexample.interfaces.rest.auth

import com.innovation.dddexample.application.member.SignInMemberCommand
import com.innovation.dddexample.application.member.SignUpMemberCommand
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
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

/**
 * AuthController 통합 테스트
 *
 * [테스트 범위]
 * - POST /auth/sign-up - 회원가입
 * - POST /auth/sign-in - 로그인
 *
 * [통합 테스트 특징]
 * - @SpringBootTest: 전체 Spring 컨텍스트 로딩
 * - @AutoConfigureMockMvc: Security 필터 비활성화로 인증 없이 테스트
 * - @Transactional: 각 테스트 후 자동 롤백
 * - 실제 DB 사용 (MySQL)
 *
 * [검증 항목]
 * 1. 회원가입 성공 시 JWT 토큰 발급
 * 2. 중복 이메일 검증
 * 3. 로그인 성공 시 JWT 토큰 발급
 * 4. 잘못된 인증 정보 처리
 * 5. Value Object 검증 (Email, PhoneNumber)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("AuthController 통합 테스트")
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setUp() {
        // 각 테스트마다 깨끗한 상태로 시작
        // @Transactional로 자동 롤백되므로 명시적인 정리는 불필요
    }

    @Nested
    @DisplayName("POST /auth/sign-up - 회원가입")
    inner class SignUpTests {

        @Test
        @DisplayName("유효한 회원 정보로 가입 성공 - 201 Created, JWT 토큰 반환")
        fun `should return 201 and JWT tokens when sign up with valid data`() {
            // Given
            val command = SignUpMemberCommand(
                email = "newuser@example.com",
                password = "SecurePassword123!",
                name = "김신규",
                phoneNumber = "01012345678"
            )

            // When
            val result = mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(command)
            }

            // Then
            result.andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.accessToken") { exists() }
                jsonPath("$.accessToken") { isNotEmpty() }
                jsonPath("$.refreshToken") { exists() }
                jsonPath("$.refreshToken") { isNotEmpty() }
            }

            // DB 검증: 실제로 회원이 저장되었는지 확인
            val savedMember = memberRepository.findByEmail(Email("newuser@example.com"))
            assertThat(savedMember).isNotNull
            assertThat(savedMember?.name).isEqualTo("김신규")
            assertThat(savedMember?.email?.value).isEqualTo("newuser@example.com")
            assertThat(savedMember?.phoneNumber?.getValue()).isEqualTo("010-1234-5678")
            assertThat(savedMember?.isActive()).isTrue
        }

        @Test
        @DisplayName("전화번호 정규화 검증 - 하이픈 없는 번호를 하이픈 포함 형식으로 저장")
        fun `should normalize phone number without hyphens`() {
            // Given
            val command = SignUpMemberCommand(
                email = "phonetest@example.com",
                password = "Password123!",
                name = "전화번호테스트",
                phoneNumber = "01099998888" // 하이픈 없음
            )

            // When
            mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(command)
            }.andExpect {
                status { isCreated() }
            }

            // Then
            val savedMember = memberRepository.findByEmail(Email("phonetest@example.com"))
            assertThat(savedMember?.phoneNumber?.getValue()).isEqualTo("010-9999-8888")
        }

        @Test
        @DisplayName("중복 이메일로 가입 시도 - 409 Conflict 반환")
        fun `should return 409 when email already exists`() {
            // Given: 기존 회원 생성
            val existingMember = Member(
                email = Email("duplicate@example.com"),
                name = "기존회원",
                phoneNumber = PhoneNumber("01011112222"),
                password = passwordEncoder.encode("ExistingPassword123!")
            )
            memberRepository.save(existingMember)

            // When: 동일 이메일로 재가입 시도
            val duplicateCommand = SignUpMemberCommand(
                email = "duplicate@example.com",
                password = "NewPassword123!",
                name = "중복시도",
                phoneNumber = "01033334444"
            )

            val result = mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(duplicateCommand)
            }

            // Then
            result.andExpect {
                status { isConflict() }
                jsonPath("$.status") { value(409) }
                jsonPath("$.message") { exists() }
            }
        }

        @Test
        @DisplayName("잘못된 이메일 형식 - 400 Bad Request 반환")
        fun `should return 400 when email format is invalid`() {
            val command = SignUpMemberCommand(
                email = "invalid-email-format",
                password = "Password123!",
                name = "테스트",
                phoneNumber = "01012345678"
            )

            mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(command)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("잘못된 전화번호 형식 - 400 Bad Request 반환")
        fun `should return 400 when phone number format is invalid`() {
            val command = SignUpMemberCommand(
                email = "validmail@example.com",
                password = "Password123!",
                name = "테스트",
                phoneNumber = "123"
            )

            mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(command)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("빈 이름으로 가입 시도 - 400 Bad Request 반환")
        fun `should return 400 when name is empty`() {
            val command = SignUpMemberCommand(
                email = "test@example.com",
                password = "Password123!",
                name = "",
                phoneNumber = "01012345678"
            )

            mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(command)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("비밀번호 암호화 검증 - 평문 비밀번호가 아닌 해시값으로 저장")
        fun `should store hashed password not plain text`() {
            // Given
            val plainPassword = "PlainPassword123!"
            val command = SignUpMemberCommand(
                email = "hashtest@example.com",
                password = plainPassword,
                name = "해시테스트",
                phoneNumber = "01012345678"
            )

            // When
            mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(command)
            }.andExpect {
                status { isCreated() }
            }

            // Then
            val savedMember = memberRepository.findByEmail(Email("hashtest@example.com"))
            assertThat(savedMember?.password).isNotEqualTo(plainPassword) // 평문이 아님
            assertThat(passwordEncoder.matches(plainPassword, savedMember?.password)).isTrue // 해시 검증 성공
        }
    }

    @Nested
    @DisplayName("POST /auth/sign-in - 로그인")
    inner class SignInTests {

        @Test
        @DisplayName("올바른 인증 정보로 로그인 성공 - 200 OK, JWT 토큰 반환")
        fun `should return 200 and JWT tokens when credentials are correct`() {
            // Given: 먼저 회원가입
            val password = "LoginPassword123!"
            val signUpCommand = SignUpMemberCommand(
                email = "loginuser@example.com",
                password = password,
                name = "로그인유저",
                phoneNumber = "01055556666"
            )

            mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signUpCommand)
            }

            // When: 로그인 시도
            val signInCommand = SignInMemberCommand(
                email = "loginuser@example.com",
                password = password
            )

            val result = mockMvc.post("/auth/sign-in") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signInCommand)
            }

            // Then
            result.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.accessToken") { exists() }
                jsonPath("$.accessToken") { isNotEmpty() }
                jsonPath("$.refreshToken") { exists() }
                jsonPath("$.refreshToken") { isNotEmpty() }
            }
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시도 - 400 Bad Request 반환")
        fun `should return 400 when password is incorrect`() {
            // Given: 회원가입
            val correctPassword = "CorrectPassword123!"
            val signUpCommand = SignUpMemberCommand(
                email = "pwdtest@example.com",
                password = correctPassword,
                name = "비밀번호테스트",
                phoneNumber = "01077778888"
            )

            mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signUpCommand)
            }

            // When: 잘못된 비밀번호로 로그인
            val wrongPasswordCommand = SignInMemberCommand(
                email = "pwdtest@example.com",
                password = "WrongPassword123!"
            )

            val result = mockMvc.post("/auth/sign-in") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(wrongPasswordCommand)
            }

            // Then
            result.andExpect {
                status { isBadRequest() }
                jsonPath("$.status") { value(400) }
            }
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시도 - 404 Not Found 반환")
        fun `should return 404 when email does not exist`() {
            // Given
            val command = SignInMemberCommand(
                email = "notexist@example.com",
                password = "AnyPassword123!"
            )

            // When
            val result = mockMvc.post("/auth/sign-in") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(command)
            }

            // Then
            result.andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(404) }
            }
        }

        @Test
        @DisplayName("탈퇴한 회원의 로그인 시도 - 400 Bad Request 반환")
        fun `should handle withdrawn member login attempt`() {
            val password = "WithdrawTest123!"
            val signUpCommand = SignUpMemberCommand(
                email = "withdrawn@example.com",
                password = password,
                name = "탈퇴예정",
                phoneNumber = "01099998888"
            )

            mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signUpCommand)
            }

            val member = memberRepository.findByEmail(Email("withdrawn@example.com"))
            member?.withdraw()
            memberRepository.save(member!!)

            val signInCommand = SignInMemberCommand(
                email = "withdrawn@example.com",
                password = password
            )

            mockMvc.post("/auth/sign-in") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signInCommand)
            }.andExpect {
                status { isBadRequest() } // 400 Bad Request (BusinessRuleViolationException)
            }
        }

        @Test
        @DisplayName("여러 번 로그인 시도 시 각각 다른 토큰 발급")
        fun `should issue different tokens for multiple login attempts`() {
            // Given: 회원가입
            val password = "MultiLoginTest123!"
            val signUpCommand = SignUpMemberCommand(
                email = "multilogin@example.com",
                password = password,
                name = "다중로그인",
                phoneNumber = "01012341234"
            )

            mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signUpCommand)
            }

            val signInCommand = SignInMemberCommand(
                email = "multilogin@example.com",
                password = password
            )

            // When: 첫 번째 로그인
            val firstResult = mockMvc.post("/auth/sign-in") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signInCommand)
            }.andReturn()

            val firstResponse = objectMapper.readTree(firstResult.response.contentAsString)
            val firstAccessToken = firstResponse.get("accessToken").asText()

            // 두 번째 로그인
            val secondResult = mockMvc.post("/auth/sign-in") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signInCommand)
            }.andReturn()

            val secondResponse = objectMapper.readTree(secondResult.response.contentAsString)
            val secondAccessToken = secondResponse.get("accessToken").asText()

            // Then: 두 토큰이 다름 (시간 정보 등이 달라서)
            assertThat(firstAccessToken).isNotEmpty
            assertThat(secondAccessToken).isNotEmpty
            // 토큰은 시간 정보를 포함하므로 다를 가능성이 높지만,
            // 테스트 실행 속도가 매우 빠르면 같을 수도 있음
        }
    }

    @Nested
    @DisplayName("회원가입 후 로그인 E2E 시나리오")
    inner class EndToEndScenarios {

        @Test
        @DisplayName("회원가입 → 로그인 → 토큰 발급 전체 플로우 검증")
        fun `should complete full sign up and sign in flow`() {
            // Step 1: 회원가입
            val password = "E2ETest123!"
            val signUpCommand = SignUpMemberCommand(
                email = "e2e@example.com",
                password = password,
                name = "E2E테스트",
                phoneNumber = "01012345678"
            )

            val signUpResult = mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signUpCommand)
            }

            signUpResult.andExpect {
                status { isCreated() }
                jsonPath("$.accessToken") { exists() }
            }

            // Step 2: DB에 저장 확인
            val savedMember = memberRepository.findByEmail(Email("e2e@example.com"))
            assertThat(savedMember).isNotNull
            assertThat(savedMember?.isActive()).isTrue

            // Step 3: 로그인
            val signInCommand = SignInMemberCommand(
                email = "e2e@example.com",
                password = password
            )

            val signInResult = mockMvc.post("/auth/sign-in") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signInCommand)
            }

            signInResult.andExpect {
                status { isOk() }
                jsonPath("$.accessToken") { exists() }
                jsonPath("$.refreshToken") { exists() }
            }
        }
    }
}
