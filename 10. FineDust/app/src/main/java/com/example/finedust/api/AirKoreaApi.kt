package com.example.finedust.api

import com.example.finedust.BuildConfig
import com.example.finedust.model.airInfo.AirInfosResponse
import com.example.finedust.model.monitoringStation.MonitoringStationsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface AirKoreaApi {

    @GET(
        "B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList" +
                "?serviceKey=${BuildConfig.AIR_KOREA_SERVICE_KEY}" +
                "&returnType=json"
    )
    suspend fun getNearMonitorStations(
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double
    ): Response<MonitoringStationsResponse>

    @GET(
        "B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty" +
                "?serviceKey=${BuildConfig.AIR_KOREA_SERVICE_KEY}" +
                "&returnType=json" +
                "&dataTerm=DAILY" +
                "&ver=1.3"
    )
    suspend fun getAirInfos(
        @Query("stationName") stationName: String
    ): Response<AirInfosResponse>

    companion object {
        private const val BASE_URL = "http://apis.data.go.kr/"

        fun create(): AirKoreaApi {
            val retrofit =
                Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(buildOkHttpClient())
                    .build()

            return retrofit.create(AirKoreaApi::class.java)
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