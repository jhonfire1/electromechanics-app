package com.example.electromechanics.data.remote

import com.example.electromechanics.data.remote.dto.LoginRequestDto
import com.example.electromechanics.data.remote.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("api/auth/register/cliente")
    suspend fun registrarCliente(@Body request: LoginRequestDto): LoginResponseDto

    @GET("api/auth/tecnicos")
    suspend fun tecnicos(@Header("Authorization") token: String): List<TecnicoDto>
}

data class TecnicoDto(val id: Long, val username: String)
