package com.innovation.dddexample.application.member

data class SignUpMemberCommand(
    val email: String,
    val name: String,
    val phoneNumber: String,
    val password: String
)