package com.innovation.dddexample.application.member

data class SignInMemberCommand(
    val email: String,
    val password: String
)