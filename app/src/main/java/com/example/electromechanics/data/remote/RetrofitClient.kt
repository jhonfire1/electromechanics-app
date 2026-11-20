package com.example.electromechanics.data.remote

import com.example.electromechanics.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Render (plan gratuito) duerme el servicio tras 15 min sin tráfico y despierta en ~1 min.
    // Con el timeout por defecto (10 s) ese arranque en frío parecería "sin internet".
    private const val TIMEOUT_SEGUNDOS = 90L

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            // BASIC registra solo método, URL y código de estado. NO usar BODY: imprimiría
            // la contraseña y el token en Logcat.
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                    redactHeader("Authorization")
                }
            )
        }
        builder.build()
    }

    val authApi: AuthApi by lazy { crear(BuildConfig.AUTH_BASE_URL, AuthApi::class.java) }
    val ordenesApi: OrdenesApi by lazy { crear(BuildConfig.OPERATIONS_BASE_URL, OrdenesApi::class.java) }

    private fun <T> crear(url: String, servicio: Class<T>): T = Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(servicio)
}
