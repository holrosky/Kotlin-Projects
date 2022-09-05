package com.example.secondhandtrade.ui.home
import java.io.Serializable

data class ItemModel(
    val sellerId: String,
    val title: String,
    val createdAt: Long,
    val price: String,
    val imgUrl: String,
    val itemDetail: String
): Serializable {
    constructor(): this("", "", 0, "", "", "")
}