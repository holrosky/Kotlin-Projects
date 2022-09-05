package com.example.secondhandtrade.ui.chat
import java.io.Serializable

data class ChatRoomModel(
    val senderId: String,
    val message: String,
    val time: Long
): Serializable {
    constructor(): this("", "", 0)
}