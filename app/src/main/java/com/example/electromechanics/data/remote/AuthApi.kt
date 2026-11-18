package com.example.electromechanics.data.remote

import com.example.electromechanics.data.remote.dto.LoginRequestDto
import com.example.electromechanics.data.remote.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
}
