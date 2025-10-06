package com.innovation.dddexample.integration

import com.innovation.dddexample.application.member.SignInMemberCommand
import com.innovation.dddexample.application.member.SignUpMemberCommand
import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.model.Member
import com.innovation.dddexample.domain.member.model.PhoneNumber
import com.innovation.dddexample.domain.member.repository.MemberRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

/**
 * Member 도메인 통합 테스트
 *
 * [통합 테스트 범위]
 * - REST API (Controller)
 * - Application Service (UseCase)
 * - Domain Service
 * - Repository (Infrastructure)
 * - Database (MySQL)
 *
 * [테스트 전략]
 * - Spring Boot 전체 컨텍스트 로딩 (@SpringBootTest)
 * - 실제 MySQL 데이터베이스 사용
 * - MockMvc를 통한 HTTP 요청 시뮬레이션
 * - 트랜잭션 롤백으로 테스트 격리 (@Transactional)
 *
 * [검증 항목]
 * 1. 회원가입 (SignUp) - Controller → UseCase → Repository → DB
 * 2. 로그인 (SignIn) - 비밀번호 검증 및 JWT 토큰 발급
 * 3. 회원 조회 (GetMember) - ID/Email 기반 조회
 * 4. 프로필 업데이트 - 도메인 로직 실행
 * 5. 회원 탈퇴 - Soft Delete 검증
 * 6. Value Object 매핑 - Email, PhoneNumber 정규화/마스킹
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)  // Security 필터 비활성화
@Transactional
@DisplayName("Member 도메인 통합 테스트")
class MemberIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Nested
    @DisplayName("회원가입 시나리오")
    inner class SignUpScenarios {

        @Test
        @DisplayName("POST /auth/signup - 성공 시 201 Created와 JWT 토큰 반환")
        fun `should return 201 with tokens when sign up succeeds`() {
            // Given
            val signUpCommand = SignUpMemberCommand(
                email = "newuser@example.com",
                password = "securePassword123!",
                name = "김철수",
                phoneNumber = "01012345678"
            )

            // When
            val result = mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signUpCommand)
            }

            // Then
            result.andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.accessToken") { exists() }
                jsonPath("$.refreshToken") { exists() }
            }

            // DB에 실제로 저장되었는지 검증
            val savedMember = memberRepository.findByEmail(Email("newuser@example.com"))
            assertThat(savedMember).isNotNull
            assertThat(savedMember?.name).isEqualTo("김철수")
            assertThat(savedMember?.phoneNumber?.getValue()).isEqualTo("010-1234-5678")
            assertThat(savedMember?.isActive()).isTrue
        }

        @Test
        @DisplayName("POST /auth/signup - 중복 이메일 시 409 Conflict 반환")
        fun `should return 409 when email already exists`() {
            // Given: 기존 회원 존재
            memberRepository.save(
                Member(
                    email = Email("duplicate@example.com"),
                    name = "기존회원",
                    phoneNumber = PhoneNumber("01099998888"),
                    password = "encodedPassword"
                )
            )

            // When: 동일 이메일로 재가입 시도
            val duplicateCommand = SignUpMemberCommand(
                email = "duplicate@example.com",
                password = "anotherPassword",
                name = "신규회원",
                phoneNumber = "01011112222"
            )

            val result = mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(duplicateCommand)
            }

            // Then
            result.andExpect {
                status { isConflict() }  // 409 Conflict
                jsonPath("$.status") { value(409) }
            }
        }
    }

    @Nested
    @DisplayName("로그인 시나리오")
    inner class SignInScenarios {

        @Test
        @DisplayName("POST /auth/signin - 올바른 인증 정보로 로그인 성공")
        fun `should return 200 with tokens when credentials are correct`() {
            // Given: 회원가입으로 사용자 생성
            val signUpCommand = SignUpMemberCommand(
                email = "logintest@example.com",
                password = "myPassword123",
                name = "로그인테스트",
                phoneNumber = "01055556666"
            )

            mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signUpCommand)
            }

            // When: 로그인 시도
            val signInCommand = SignInMemberCommand(
                email = "logintest@example.com",
                password = "myPassword123"
            )

            val result = mockMvc.post("/auth/sign-in") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signInCommand)
            }

            // Then
            result.andExpect {
                status { isOk() }
                jsonPath("$.accessToken") { exists() }
                jsonPath("$.refreshToken") { exists() }
            }
        }

        @Test
        @DisplayName("POST /auth/signin - 잘못된 비밀번호로 400 Bad Request 반환")
        fun `should return 400 when password is incorrect`() {
            // Given: 회원가입
            val signUpCommand = SignUpMemberCommand(
                email = "pwdtest@example.com",
                password = "correctPassword",
                name = "비밀번호테스트",
                phoneNumber = "01012341234"
            )

            mockMvc.post("/auth/sign-up") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(signUpCommand)
            }

            // When: 잘못된 비밀번호로 로그인
            val wrongPasswordCommand = SignInMemberCommand(
                email = "pwdtest@example.com",
                password = "wrongPassword"
            )

            val result = mockMvc.post("/auth/sign-in") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(wrongPasswordCommand)
            }

            // Then
            result.andExpect {
                status { isBadRequest() }  // 400 Bad Request
                jsonPath("$.status") { value(400) }
            }
        }

        @Test
        @DisplayName("POST /auth/signin - 존재하지 않는 이메일로 404 Not Found 반환")
        fun `should return 404 when email does not exist`() {
            // When
            val notFoundCommand = SignInMemberCommand(
                email = "notfound@example.com",
                password = "anyPassword"
            )

            val result = mockMvc.post("/auth/sign-in") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(notFoundCommand)
            }

            // Then
            result.andExpect {
                status { isNotFound() }
            }
        }
    }

    @Nested
    @DisplayName("회원 조회 시나리오")
    inner class GetMemberScenarios {

        @Test
        @DisplayName("GET /members/{id} - 존재하는 회원 조회 성공")
        fun `should return 200 with member data when member exists`() {
            // Given
            val member = memberRepository.save(
                Member(
                    email = Email("querytest@example.com"),
                    name = "조회테스트",
                    phoneNumber = PhoneNumber("01077778888"),
                    password = "hashedPassword"
                )
            )

            // When
            val result = mockMvc.get("/members/${member.id}")

            // Then
            result.andExpect {
                status { isOk() }
                jsonPath("$.id") { value(member.id) }
                jsonPath("$.name") { value("조회테스트") }
                jsonPath("$.email") { value("querytest@example.com") }
                jsonPath("$.phoneNumber") { exists() } // 마스킹된 값이 존재하는지만 확인
                jsonPath("$.status") { value("ACTIVE") }
            }
        }

        @Test
        @DisplayName("GET /members/{id} - 존재하지 않는 ID로 404 Not Found 반환")
        fun `should return 404 when member does not exist`() {
            // When
            val result = mockMvc.get("/members/99999")

            // Then
            result.andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(404) }
                jsonPath("$.message") { value("Member not found with id: 99999") }
            }
        }
    }

    @Nested
    @DisplayName("회원 프로필 업데이트 시나리오")
    inner class UpdateProfileScenarios {

        @Test
        @DisplayName("회원 프로필(이름, 전화번호) 업데이트 성공")
        fun `should update member profile successfully`() {
            // Given
            val member = memberRepository.save(
                Member(
                    email = Email("updatetest@example.com"),
                    name = "업데이트전",
                    phoneNumber = PhoneNumber("01011112222"),
                    password = "hashedPassword"
                )
            )

            // When: 도메인 로직 실행
            val updatedMember = memberRepository.findById(member.id!!)
            updatedMember?.updateProfile(
                name = "업데이트후",
                phoneNumber = PhoneNumber("01033334444")
            )
            memberRepository.save(updatedMember!!)

            // Then
            val result = memberRepository.findById(member.id!!)
            assertThat(result?.name).isEqualTo("업데이트후")
            assertThat(result?.phoneNumber?.getValue()).isEqualTo("010-3333-4444")
        }

        @Test
        @DisplayName("회원 이메일 변경 성공")
        fun `should change email successfully`() {
            // Given
            val member = memberRepository.save(
                Member(
                    email = Email("oldemail@example.com"),
                    name = "이메일변경테스트",
                    phoneNumber = PhoneNumber("01012341234"),
                    password = "hashedPassword"
                )
            )

            // When
            val updatedMember = memberRepository.findById(member.id!!)
            updatedMember?.changeEmail(Email("newemail@example.com"))
            memberRepository.save(updatedMember!!)

            // Then
            val result = memberRepository.findById(member.id!!)
            assertThat(result?.email?.value).isEqualTo("newemail@example.com")

            // 새 이메일로 조회 가능
            val foundByEmail = memberRepository.findByEmail(Email("newemail@example.com"))
            assertThat(foundByEmail).isNotNull
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 시나리오")
    inner class WithdrawScenarios {

        @Test
        @DisplayName("회원 탈퇴 시 withdrawnAt 설정 및 상태 변경")
        fun `should set withdrawnAt and change status when withdrawing`() {
            // Given
            val member = memberRepository.save(
                Member(
                    email = Email("withdraw@example.com"),
                    name = "탈퇴예정",
                    phoneNumber = PhoneNumber("01022223333"),
                    password = "hashedPassword"
                )
            )

            // When
            val targetMember = memberRepository.findById(member.id!!)
            targetMember?.withdraw()
            memberRepository.save(targetMember!!)

            // Then
            val result = memberRepository.findById(member.id!!)
            assertThat(result?.isWithdrawn()).isTrue
            assertThat(result?.isActive()).isFalse
            assertThat(result?.withdrawnAt).isNotNull
        }

        @Test
        @DisplayName("탈퇴한 회원은 API에서 WITHDRAWN 상태로 반환")
        fun `should return WITHDRAWN status for withdrawn member`() {
            // Given
            val member = memberRepository.save(
                Member(
                    email = Email("withdrawn-api@example.com"),
                    name = "탈퇴회원",
                    phoneNumber = PhoneNumber("01044445555"),
                    password = "hashedPassword"
                )
            )

            val targetMember = memberRepository.findById(member.id!!)
            targetMember?.withdraw()
            memberRepository.save(targetMember!!)

            // When
            val apiResult = mockMvc.get("/members/${member.id}")

            // Then
            apiResult.andExpect {
                status { isOk() }
                jsonPath("$.status") { value("WITHDRAWN") }
            }
        }

        @Test
        @DisplayName("이미 탈퇴한 회원이 재탈퇴 시 예외 발생")
        fun `should throw exception when trying to withdraw already withdrawn member`() {
            // Given
            val member = memberRepository.save(
                Member(
                    email = Email("double-withdraw@example.com"),
                    name = "이중탈퇴테스트",
                    phoneNumber = PhoneNumber("01066667777"),
                    password = "hashedPassword"
                )
            )

            val targetMember = memberRepository.findById(member.id!!)
            targetMember?.withdraw()
            memberRepository.save(targetMember!!)

            // When & Then
            val reloadedMember = memberRepository.findById(member.id!!)
            assertThatThrownBy {
                reloadedMember?.withdraw()
            }.hasMessageContaining("이미 탈퇴한 회원입니다")
        }
    }

    @Nested
    @DisplayName("Value Object 매핑 검증 시나리오")
    inner class ValueObjectMappingScenarios {

        @Test
        @DisplayName("하이픈 없는 전화번호 저장 시 정규화된 형식으로 저장")
        fun `should normalize phone number without hyphens`() {
            // Given & When
            val phoneNumber = PhoneNumber("01099998888") // 하이픈 없음
            val member = memberRepository.save(
                Member(
                    email = Email("valueobject@test.com"),
                    name = "값객체테스트",
                    phoneNumber = phoneNumber,
                    password = "hashedPassword"
                )
            )

            // Then: PhoneNumber는 생성 시 정규화되므로 하이픈이 추가됨
            val result = memberRepository.findById(member.id!!)
            assertThat(result?.phoneNumber?.getValue()).isEqualTo("010-9999-8888")
        }

        @Test
        @DisplayName("하이픈이 있는 전화번호는 동일한 형식으로 유지")
        fun `should keep phone number with hyphens as is`() {
            // Given & When
            val member = memberRepository.save(
                Member(
                    email = Email("hyphenated@test.com"),
                    name = "하이픈테스트",
                    phoneNumber = PhoneNumber("010-1111-2222"), // 하이픈 있음
                    password = "hashedPassword"
                )
            )

            // Then
            val result = memberRepository.findById(member.id!!)
            assertThat(result?.phoneNumber?.getValue()).isEqualTo("010-1111-2222")
        }

        @Test
        @DisplayName("Email Value Object는 저장 시 정확히 매핑됨")
        fun `should map email value object correctly`() {
            // Given & When
            val email = Email("test@example.com")
            val member = memberRepository.save(
                Member(
                    email = email,
                    name = "이메일테스트",
                    phoneNumber = PhoneNumber("01012345678"),
                    password = "hashedPassword"
                )
            )

            // Then
            val result = memberRepository.findById(member.id!!)
            assertThat(result?.email?.value).isEqualTo("test@example.com")

            // 이메일로 조회 가능
            val found = memberRepository.findByEmail(Email("test@example.com"))
            assertThat(found).isNotNull
        }
    }

    @Nested
    @DisplayName("Repository CRUD 통합 검증")
    inner class RepositoryCrudScenarios {

        @Test
        @DisplayName("save() - 회원 저장 시 ID 자동 생성")
        fun `should auto-generate ID when saving member`() {
            // Given
            val member = Member(
                email = Email("crud@test.com"),
                name = "CRUD테스트",
                phoneNumber = PhoneNumber("01055556666"),
                password = "hashedPassword"
            )

            // When
            val saved = memberRepository.save(member)

            // Then
            assertThat(saved.id).isNotNull
            assertThat(saved.id!!).isGreaterThan(0)
        }

        @Test
        @DisplayName("findById() - ID로 회원 조회 성공")
        fun `should find member by ID`() {
            // Given
            val saved = memberRepository.save(
                Member(
                    email = Email("findbyid@test.com"),
                    name = "ID조회",
                    phoneNumber = PhoneNumber("01066667777"),
                    password = "hashedPassword"
                )
            )

            // When
            val found = memberRepository.findById(saved.id!!)

            // Then
            assertThat(found).isNotNull
            assertThat(found?.id).isEqualTo(saved.id)
            assertThat(found?.email).isEqualTo(saved.email)
        }

        @Test
        @DisplayName("findByEmail() - 이메일로 회원 조회 성공")
        fun `should find member by email`() {
            // Given
            val email = Email("findbyemail@test.com")
            memberRepository.save(
                Member(
                    email = email,
                    name = "이메일조회",
                    phoneNumber = PhoneNumber("01077778888"),
                    password = "hashedPassword"
                )
            )

            // When
            val found = memberRepository.findByEmail(email)

            // Then
            assertThat(found).isNotNull
            assertThat(found?.email).isEqualTo(email)
        }

        @Test
        @DisplayName("findByEmail() - 존재하지 않는 이메일은 null 반환")
        fun `should return null when email does not exist`() {
            // When
            val found = memberRepository.findByEmail(Email("notexist@test.com"))

            // Then
            assertThat(found).isNull()
        }

        @Test
        @DisplayName("deleteById() - 회원 삭제 성공")
        fun `should delete member by ID`() {
            // Given
            val saved = memberRepository.save(
                Member(
                    email = Email("delete@test.com"),
                    name = "삭제테스트",
                    phoneNumber = PhoneNumber("01088889999"),
                    password = "hashedPassword"
                )
            )

            // When
            memberRepository.deleteById(saved.id!!)

            // Then
            val found = memberRepository.findById(saved.id!!)
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("동시성 시나리오")
    inner class ConcurrencyScenarios {

        @Test
        @DisplayName("여러 회원 저장 시 모두 고유한 ID를 가짐")
        fun `should assign unique IDs to multiple members`() {
            // Given & When
            val members = (1..5).map { i ->
                memberRepository.save(
                    Member(
                        email = Email("user$i@test.com"),
                        name = "사용자$i",
                        phoneNumber = PhoneNumber("010${String.format("%08d", i)}"),
                        password = "hashedPassword"
                    )
                )
            }

            // Then
            val ids = members.map { it.id }
            assertThat(ids.distinct()).hasSize(5)
        }

        @Test
        @DisplayName("각 회원을 이메일로 조회 가능")
        fun `should be able to find each member by email`() {
            // Given
            val members = (1..3).map { i ->
                memberRepository.save(
                    Member(
                        email = Email("concurrent$i@test.com"),
                        name = "동시성$i",
                        phoneNumber = PhoneNumber("010${String.format("%08d", i)}"),
                        password = "hashedPassword"
                    )
                )
            }

            // When & Then
            members.forEach { member ->
                val found = memberRepository.findByEmail(member.email)
                assertThat(found).isNotNull
                assertThat(found?.id).isEqualTo(member.id)
            }
        }
    }
}
