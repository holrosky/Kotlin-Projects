package com.example.finedust.model.airInfo

import androidx.annotation.ColorRes
import com.example.finedust.R
import com.google.gson.annotations.SerializedName

enum class AirGrade(
    val label: String,
    val emoji: String,
    @ColorRes val colorResId: Int
) {
    @SerializedName("1")
    GOOD("μ’μ", "π", R.color.blue),

    @SerializedName("2")
    NORMAL("λ³΄ν΅", "βΊοΈ", R.color.green),

    @SerializedName("3")
    BAD("λμ¨", "π", R.color.yellow),

    @SerializedName("4")
    AWFUL("λ§€μ° λμ¨", "π‘", R.color.red),

    UNKNOWN("λ―ΈμΈ‘μ ", "π§", R.color.gray);

    override fun toString(): String {
        return "$label $emoji"
    }
}