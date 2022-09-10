package com.kotlin_project.locationsearch.repository

import com.kotlin_project.locationsearch.api.PoiApi

class PoiRepository {
    private val api = PoiApi.create()

    suspend fun getKeywordSearch(keyword: String,
                                 appKey: String) = api.getKeyowrdSearch(keyword = keyword, appKey = appKey)

    suspend fun getLocationInfoByLatLon(lat: Double, lon: Double,
                                appKey: String) = api.getLocationInfoByLatLon(lat = lat, lon = lon, appKey = appKey)
}