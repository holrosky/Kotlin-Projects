package com.example.finedust.repository

import com.example.finedust.api.AirKoreaApi
import com.example.finedust.api.KakaoLoactionApi
import com.example.finedust.model.airInfo.AirInfo
import com.example.finedust.model.monitoringStation.MonitoringStation
import com.example.finedust.model.tmCorrdinate.Document

class Repository {
    private val kakaoApi = KakaoLoactionApi.create()
    private val airKoreaApi = AirKoreaApi.create()

    suspend fun getTmCoordinates(longitude: Double, latitude: Double): Document? =
        kakaoApi.getTmCoordinates(longitude = longitude, latitude = latitude).body()
            ?.documents
            ?.firstOrNull()

    suspend fun getNearMonitorStations(tmX: Double, tmY: Double): MonitoringStation? =
        airKoreaApi.getNearMonitorStations(tmX = tmX, tmY = tmY).body()
            ?.response
            ?.body
            ?.monitoringStations
            ?.minByOrNull {
                it?.tm ?: Double.MAX_VALUE
            }

    suspend fun getAirInfos(stationName: String): AirInfo? =
        airKoreaApi.getAirInfos(stationName = stationName).body()
            ?.response
            ?.body
            ?.airInfos
            ?.firstOrNull()

}