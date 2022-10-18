package com.example.finedust.api

import com.example.finedust.BuildConfig
import com.example.finedust.model.tmCorrdinate.TmCoordinateReponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface KakaoLoactionApi {

    @Headers("Authorization: KakaoAK ${BuildConfig.KAKAO_API_KEY}")
    @GET("/v2/local/geo/transcoord.json?output_coord=TM")
    suspend fun getTmCoordinates(
        @Query("x") longitude: Double,
        @Query("y") latitude: Double
    ): Response<TmCoordinateReponse>

    companion object {
        private const val BASE_URL = "https://dapi.kakao.com/"

        fun create(): KakaoLoactionApi {
            val retrofit =
                Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(buildOkHttpClient())
                    .build()

            return retrofit.create(KakaoLoactionApi::class.java)
        }

        private fun buildOkHttpClient(): OkHttpClient {
            val interceptor = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG) {
                interceptor.level = HttpLoggingInterceptor.Level.BODY
            } else {
                interceptor.level = HttpLoggingInterceptor.Level.NONE
            }
            return OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build()
        }
    }
}