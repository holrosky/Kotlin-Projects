package com.kotlin_project.melon.adapter

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.kotlin_project.melon.R
import java.util.concurrent.TimeUnit

object BindingAdapter {

    @BindingAdapter("imageUrl")
    @JvmStatic
    fun loadImage(imageView: ImageView, url: String?) {
        if (url == null)
            return

        Glide.with(imageView.context).load(url)
            .into(imageView)
    }

    @BindingAdapter("controlButtonImage")
    @JvmStatic
    fun loadControlButtonImage(imageView: ImageView, isPlaying: Boolean) {

        if (isPlaying)
            imageView.setImageResource(R.drawable.ic_pause_48)
        else
            imageView.setImageResource(R.drawable.ic_play_48)
    }

    @BindingAdapter("timeText")
    @JvmStatic
    fun setTimeText(textView: TextView, time: Long) {
        textView.text = String.format(
            "%02d:%02d",
            TimeUnit.MINUTES.convert(time, TimeUnit.MILLISECONDS),
            (time / 1000) % 60
        )
    }
}