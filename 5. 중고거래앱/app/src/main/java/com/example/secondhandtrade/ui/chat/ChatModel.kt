package com.example.secondhandtrade.ui.chat
import java.io.Serializable

data class ChatModel(
    val buyerId: String,
    val sellerId: String,
    val imgUrl: String,
    val title: String,
    val type: String,
    val price: String,
    val key: Long
): Serializable {
    constructor(): this("",  "", "","", "","", 0)
}