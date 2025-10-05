package com.innovation.dddexample.domain.member.repository

import com.innovation.dddexample.domain.member.model.Email
import com.innovation.dddexample.domain.member.model.Member

interface MemberRepository {
    /**
     * 회원을 저장하거나 수정합니다. (생성 및 업데이트)
     */
    fun save(member: Member): Member

    /**
     * 고유 ID로 회원을 찾습니다.
     */
    fun findById(id: Long): Member?

    /**
     * 이메일로 회원을 찾습니다. (로그인, 이메일 중복 확인에 사용)
     */
    fun findByEmail(email: Email): Member?

    /**
     * 고유 ID로 회원을 삭제합니다. (회원 탈퇴)
     */
    fun deleteById(id: Long)
}