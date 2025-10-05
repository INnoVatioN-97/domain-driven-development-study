package com.innovation.dddexample.interfaces.dto.auth

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,

    )