package ru.dyringleb.librereaderapp.retrofitConf

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.dyringleb.librereaderapp.SharedPreferencesHelper


object RetrofitClient {
    private const val EMULATOR = "http://10.0.2.2:8000/"
    private const val RELEASE = "http://192.168.0.21:8000/"
    const val BASE_URL = EMULATOR
    private lateinit var sharedPreferences: SharedPreferencesHelper

    fun initialize(sharedPreferencesHelper: SharedPreferencesHelper) {
        sharedPreferences = sharedPreferencesHelper
    }

    class AuthInterceptor(private val sharedPreferences: SharedPreferencesHelper) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val tokenValue: String? = sharedPreferences.getAuthToken()

            // Проверяем, что токен не пустой
            if (!tokenValue.isNullOrBlank()) {
                val token = "Token $tokenValue" // Получаем токен из SharedPreferences

                val originalRequest: Request = chain.request()
                val requestWithAuth = originalRequest.newBuilder()
                    .header("Authorization", token)
                    .build()
                return chain.proceed(requestWithAuth)
            } else {
                // Если токен отсутствует, просто продолжаем с оригинальным запросом
                return chain.proceed(chain.request())
            }
        }
    }


    // Создание клиент с передачей SharedPreferences
    private val okHttpClient: OkHttpClient by lazy {
        if (!::sharedPreferences.isInitialized) {
            throw IllegalStateException("RetrofitClient not initialized. Call initialize() with SharedPreferencesHelper first.")
        }
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sharedPreferences)) // Передаем sharedPreferences
            .build()
    }

    // Создание экземпляра Retrofit
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Используем наш OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
