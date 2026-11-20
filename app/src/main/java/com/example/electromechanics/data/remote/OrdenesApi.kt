package com.example.electromechanics.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class CrearOrdenDto(
    val id: String,
    val tecnicoId: Long,
    val cliente: String,
    val direccion: String,
    val descripcion: String,
    val fecha: String
)

data class OrdenDto(
    val id: String,
    val tecnicoId: Long,
    val cliente: String,
    val direccion: String,
    val descripcion: String,
    val fecha: String,
    val estado: String
)

data class CambioEstadoDto(val estado: String)

interface OrdenesApi {
    @POST("api/ordenes")
    suspend fun crear(@Header("Authorization") token: String, @Body orden: CrearOrdenDto): OrdenDto

    @GET("api/ordenes/asignadas")
    suspend fun asignadas(@Header("Authorization") token: String): List<OrdenDto>

    @PUT("api/ordenes/{id}/estado")
    suspend fun cambiarEstado(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body cambio: CambioEstadoDto
    ): OrdenDto
}
