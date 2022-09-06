package com.kotlin_project.airbnb.binding

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

object BindingConversions {

    @BindingAdapter("imageUrl", "error")
    @JvmStatic
    fun loadImage(imageView: ImageView, url: String, error: Drawable) {

        Glide.with(imageView.context).load(url)
            .error(error)
            .into(imageView)
    }

    @BindingAdapter("roundImageUrl", "error")
    @JvmStatic
    fun loadRoundImage(imageView: ImageView, url: String, error: Drawable) {

        Glide.with(imageView.context).load(url)
            .error(error)
            .transform(CenterCrop(), RoundedCorners(dpToPx(imageView.context, 12)))
            .into(imageView)
    }

    @JvmStatic
    private fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }

}