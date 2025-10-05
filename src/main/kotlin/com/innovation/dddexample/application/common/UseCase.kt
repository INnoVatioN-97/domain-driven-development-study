package com.innovation.dddexample.application.common

/**
 * 모든 유스케이스가 구현해야 하는 공통 인터페이스입니다.
 * Command(입력)를 받아 Result(출력)를 반환하는 단일 `execute` 메서드를 정의합니다.
 *
 * @param C Command 타입
 * @param R Result 타입
 */
fun interface UseCase<C, R> {
    fun execute(command: C): R
}
