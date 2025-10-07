package com.innovation.dddexample.interfaces.rest.auth

import com.innovation.dddexample.application.member.SignInMemberCommand
import com.innovation.dddexample.application.member.SignInMemberUseCase
import com.innovation.dddexample.application.member.SignUpMemberCommand
import com.innovation.dddexample.application.member.SignUpMemberUseCase
import com.innovation.dddexample.interfaces.dto.auth.TokenResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val signUpMemberUseCase: SignUpMemberUseCase,
    private val signInMemberUseCase: SignInMemberUseCase
) {

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    fun userSignUp(@RequestBody request: SignUpMemberCommand): TokenResponse {
        return signUpMemberUseCase.execute(request)
    }

    @PostMapping("/sign-in")
    fun userSignIn(@RequestBody command: SignInMemberCommand): TokenResponse {
        return signInMemberUseCase.execute(command)
    }

}