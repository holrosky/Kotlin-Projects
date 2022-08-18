package com.kotlin_project.alarm

data class AlarmDataModel(
    val hour: Int,
    val minute: Int,
    var onOff: Boolean
) {
    val timeText: String
        get() {
            val h = if (hour > 12) hour - 12 else hour
            val m = minute

            return "${"%02d".format(h)}:${"%02d".format(m)}"
        }

    val ampmText: String
        get() {
            return if (hour >= 12) "PM" else "AM"
        }

    val onOffText: String
        get() {
            return if (onOff) "알람 켜짐" else "알람 꺼짐"
        }

    fun makeDataForDB(): String {
        return "$hour:$minute"
    }
}
