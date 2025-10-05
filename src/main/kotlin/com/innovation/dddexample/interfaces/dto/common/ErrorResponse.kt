package com.innovation.dddexample.interfaces.dto.common

/**
 * API 에러 응답 DTO입니다.
 *
 * [공통 에러 응답 형식]
 * - message: 사용자에게 보여줄 에러 메시지
 * - status: HTTP 상태 코드 (404, 400 등)
 *
 * [향후 확장 가능]
 * - timestamp: 에러 발생 시각
 * - path: 요청 경로
 * - errorCode: 애플리케이션 정의 에러 코드
 * - details: 상세 에러 정보 (필드 검증 오류 등)
 */
data class ErrorResponse(
    val message: String,
    val status: Int
)
