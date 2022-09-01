package com.example.secondhandtrade.ui.home


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.secondhandtrade.Constant.DEFAULT_IMG_URI
import com.example.secondhandtrade.databinding.ItemBinding
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ItemAdapter : ListAdapter<ItemModel, ItemAdapter.ViewHolder>(diffUtil) {
    inner class ViewHolder(private val binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemModel: ItemModel) {
            val format = SimpleDateFormat("MM월 dd일")
            val date = Date(itemModel.createdAt)

            binding.titleTextView.text = itemModel.title
            binding.dateTextView.text = format.format(date).toString()

            val formatter: NumberFormat = DecimalFormat("#,###")
            val formattedPrice: String = formatter.format(itemModel.price.toLong())
            binding.priceTextView.text = "${formattedPrice}원"

            var imgUri = itemModel.imgUrl

            if (imgUri.isEmpty())
                imgUri = DEFAULT_IMG_URI

            Glide.with(binding.thumbnailImageView)
                .load(imgUri)
                .into(binding.thumbnailImageView)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ItemModel>() {
            override fun areItemsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
                return oldItem.createdAt == newItem.createdAt
            }

        }
    }
}