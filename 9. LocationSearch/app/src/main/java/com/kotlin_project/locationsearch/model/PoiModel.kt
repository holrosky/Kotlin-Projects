package com.kotlin_project.locationsearch.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class SearchPoiInfoModel (
    val searchPoiInfo: PoisModel
)

data class PoisModel (
    val pois: PoiModel
)

data class PoiModel (
    val poi: List<ItemModel>
)

@Parcelize
data class ItemModel(
    val id: String? = null,
    val name: String? = null,
    val telNo: String? = null,
    val frontLat: Float = 0.0f,
    val frontLon: Float = 0.0f,
    val noorLat: Float = 0.0f,
    val noorLon: Float = 0.0f,
    val upperAddrName: String? = null,
    val middleAddrName: String? = null,
    val lowerAddrName: String? = null,
    val detailAddrName: String? = null,
    val firstNo: String? = null,
    val secondNo: String? = null,
    val roadName: String? = null,
    val firstBuildNo: String? = null,
    val secondBuildNo: String? = null,
    val mlClass: String? = null,
    val radius: String? = null,
    val bizName: String? = null,
    val upperBizName: String? = null,
    val middleBizName: String? = null,
    val lowerBizName: String? = null,
    val detailBizName: String? = null,
    val rpFlag: String? = null,
    val parkFlag: String? = null,
    val detailInfoFlag: String? = null,
    val desc: String? = null
) : Parcelable
