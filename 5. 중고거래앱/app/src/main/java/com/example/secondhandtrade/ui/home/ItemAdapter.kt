package com.example.secondhandtrade.ui.home


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

class ItemAdapter(val onItemClicked: (ItemModel) -> Unit) : ListAdapter<ItemModel, ItemAdapter.ViewHolder>(diffUtil) {
    inner class ViewHolder(private val binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemModel: ItemModel) {
            val format = SimpleDateFormat("MM월 dd일")
            val date = Date(itemModel.createdAt)

            binding.titleTextView.text = itemModel.title
            binding.dateTextView.text = format.format(date).toString()
            binding.root.setOnClickListener {
                onItemClicked(itemModel)
            }

            if(itemModel.price == "0") {
                binding.priceTextView.text = "나눔"
                binding.priceTextView.setTextColor(Color.parseColor("#FF9A1E"))
            } else {
                val formatter: NumberFormat = DecimalFormat("#,###")
                val formattedPrice: String = formatter.format(itemModel.price.toLong())
                binding.priceTextView.text = "${formattedPrice}원"
            }

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