package com.kotlin_project.airbnb.adapter

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kotlin_project.airbnb.R
import com.kotlin_project.airbnb.binding.ItemClickListener
import com.kotlin_project.airbnb.databinding.ItemViewPagerAccommodationBinding
import com.kotlin_project.airbnb.model.AccommodationModel

class AccommodationViewPagerAdapter :
    ListAdapter<AccommodationModel, AccommodationViewPagerAdapter.ItemViewHolder>(diffUtil) {

    private var listener: ItemClickListener? = null

    inner class ItemViewHolder(private val binding: ItemViewPagerAccommodationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(accommodationModel: AccommodationModel) {
            binding.accommodationModel = accommodationModel

            binding.viewPagerCardView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    if (listener == null) return@setOnClickListener
                    listener?.sendValue(accommodationModel, pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = DataBindingUtil.inflate<ItemViewPagerAccommodationBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_view_pager_accommodation, parent, false
        )

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    fun itemClickListener(listener: ItemClickListener) {
        this.listener = listener
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<AccommodationModel>() {
            override fun areItemsTheSame(
                oldItem: AccommodationModel,
                newItem: AccommodationModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: AccommodationModel,
                newItem: AccommodationModel
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}