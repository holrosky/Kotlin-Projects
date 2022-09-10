package com.kotlin_project.locationsearch.api

import com.kotlin_project.locationsearch.BuildConfig
import com.kotlin_project.locationsearch.model.AddressInfoModel
import okhttp3.logging.HttpLoggingInterceptor
import com.kotlin_project.locationsearch.model.SearchPoiInfoModel
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface PoiApi {
    @GET("/tmap/pois")
    suspend fun getKeyowrdSearch(
        @Query("version") version: Int = 1,
        @Query("searchKeyword") keyword: String,
        @Header("appKey") appKey: String,
        @Query("count") count: Int = 20
    ): Response<SearchPoiInfoModel>

    @GET("/tmap/geo/reversegeocoding")
    suspend fun getLocationInfoByLatLon(
        @Header("appKey") appKey: String,
        @Query("version") version: Int = 1,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<AddressInfoModel>

    companion object {
        private const val BASE_URL = "https://apis.openapi.sk.com"

        fun create(): PoiApi {
             val retrofit =
                Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(buildOkHttpClient())
                    .build()


            return retrofit.create(PoiApi::class.java)
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