package com.example.secondhandtrade.ui.home

data class ItemModel(
    val sellerId: String,
    val title: String,
    val createdAt: Long,
    val price: String,
    val imgUrl: String,
    val itemDetail: String
) {
    constructor(): this("", "", 0, "", "", "")
}